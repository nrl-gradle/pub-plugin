package nrlssc.gradle

import nrlssc.gradle.extensions.PubExtension
import nrlssc.gradle.tasks.PublishYumTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PubPlugin implements Plugin<Project>{
    private static Logger logger = LoggerFactory.getLogger(PubPlugin.class)

    public static final String PUB_GROUP = 'publishing'
    public static final String YUM_CONFIG = 'yumArchives'
    public static final String DIST_CONFIG = 'distributions'


    @Override
    void apply(Project project) {
        project.extensions.create("pub", PubExtension, project)
        project.pluginManager.apply('maven-publish')
        project.pluginManager.apply('ivy-publish')

        Configuration yumConfig = project.configurations.create(YUM_CONFIG)
        project.configurations.add(yumConfig)

        PublishYumTask ptask = PublishYumTask.createFor(project)
        project.tasks.getByName('publish').dependsOn(ptask)

        project.gradle.taskGraph.whenReady {
            for (Task tsk : project.gradle.taskGraph.getAllTasks()) {
                if (tsk.name.equalsIgnoreCase("publish")) {
                    if(project.name == project.rootProject.name) {
                        PubExtension ext = project.extensions.getByType(PubExtension)
                        logger.lifecycle("Publishing $project.name to " + ext.getPublishType() + " repositories.")
                    }
                }
            }
        }

        project.gradle.taskGraph.afterTask { Task task, TaskState state ->
            if(task.name.startsWith("publish") && task.name != "publish" && task.project == project)
            {
                if (!state.failure) {
                    String list = ""
                    task.inputs.files.each{ ip ->
                        list += "\t" + ip.name + "\n"
                    }

                    if(list.length() > 0) println("Successfully published artifacts: \n" + list)
                }
            }
        }

        Configuration distConf = project.configurations.create(DIST_CONFIG)
        project.configurations.add(distConf)

        project.gradle.projectsEvaluated {
            PubExtension ext = project.extensions.getByType(PubExtension)
            try {
                ext.publishType = project.hgit.isReleaseBranch(project.hgit.fetchBranch()) ? 'release' : 'snapshot'
            }catch(Exception ex){}
            ext.selectRepos()

            project.publishing.publications{
                mavenJava(MavenPublication){
                    from project.components.java

                    List<PublishArtifact> handled = new ArrayList<>()

                    project.configurations.distributions.artifacts.each{art ->
                        if(!handled.contains(art)) {
                            artifact(art) {
                                classifier art.classifier
                            }
                            handled.add(art)
                        }
                    }

                    boolean skip = true
                    project.configurations.archives.artifacts.each{art ->
                        if(!skip) {
                            if (!handled.contains(art)) {
                                artifact(art) {
                                    classifier art.classifier
                                }
                                handled.add(art)
                            }
                        }
                        else
                        {
                            skip = false
                        }
                    }



                    versionMapping {
                        usage('java-api') {
                            fromResolutionOf('runtimeClasspath')
                        }
                        usage('java-runtime') {
                            fromResolutionResult()
                        }
                    }
                }
                ivyJava(IvyPublication){
                    from project.components.java

                    configurations{
                        distributions{}
                    }

                    List<PublishArtifact> handled = new ArrayList<>()
                    project.configurations.distributions.artifacts.each{art ->
                        if(!handled.contains(art) && ! art instanceof LazyPublishArtifact) {
                            artifact(art) {
                                conf DIST_CONFIG
                            }
                            handled.add(art)
                        }
                    }

                    boolean skip = true
                    project.configurations.archives.artifacts.each{art ->

                        if(!skip) {
                            if(!handled.contains(art) && ! art instanceof LazyPublishArtifact) {
                                artifact art
                                handled.add(art)
                            }
                        }
                        else{
                            skip = false
                        }
                    }

                    versionMapping {
                        usage('java-api') {
                            fromResolutionOf('runtimeClasspath')
                        }
                        usage('java-runtime') {
                            fromResolutionResult()
                        }
                    }
                }
            }
        }
        
    }
}
