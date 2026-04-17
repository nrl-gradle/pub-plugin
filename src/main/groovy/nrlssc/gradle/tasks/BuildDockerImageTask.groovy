package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import nrlssc.gradle.extensions.RepoConfig
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskState

/**
 * Created by scraft on 10/01/2024.
 */
class BuildDockerImageTask extends DockerTask {

    static BuildDockerImageTask createFor(Project project)
    {
        BuildDockerImageTask task = project.tasks.create("buildDockerImage", BuildDockerImageTask.class)
        task.group = PubPlugin.PUB_GROUP
        task.description = 'Creates a Docker Image and tags it, using local docker commands (must be on PATH)'

        task.onlyIf {
            String df = 'Dockerfile'
            if(task.dockerfile != null){
                df = task.dockerfile
            }
            File dfile = project.file(task.contextPath + '/' + df)
            dfile.exists()
        }

        return task
    }

    @Input
    String contextPath = "."
    @Input
    @Optional
    String dockerfile = null

    void contextPath(String path){
        this.contextPath = path
    }

    void dockerfile(String fileName){
        this.dockerfile = fileName
    }



    @TaskAction
    void run()    {

        logger.info("Building docker images")
        String msg = "Successfully built docker image for " + project.getName() + " with tags:\n"
        String cmd = "docker build "



        for(String tag in getTags()){
            cmd = cmd + "--tag $tag "
            msg += "    $tag\n"
        }
        PubExtension pubExt = project.extensions.getByType(PubExtension.class)

        boolean doRun = false

        for(PubConfig pubConfig : pubExt.getPubConfigs())
        {
            for(RepoConfig repoConfig : pubConfig.getDockerRepos()) {
                String repoKey = repoConfig.key
                doRun = true
                dockerLogin(pubConfig.username, pubConfig.password, repoKey)
            }
        }



        if(!doRun){
            logger.info("Skipping docker build")
            this.didWork = false
        }
        else {
            logger.debug('docker build')
            if(dockerfile != null){
                cmd += " -f $dockerfile"
            }
            cmd += " " + contextPath

            execute(cmd, null, "ERROR:")
            println(msg)
            this.didWork = true
        }
    }
}
