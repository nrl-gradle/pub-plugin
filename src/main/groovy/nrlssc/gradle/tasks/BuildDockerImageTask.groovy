package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
import nrlssc.gradle.extensions.DockerConfig
import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

/**
 * Created by scraft on 10/01/2024.
 */
class BuildDockerImageTask extends DockerTask {

    static BuildDockerImageTask createFor(Project project)
    {
        BuildDockerImageTask task = project.tasks.create("buildDockerImage", BuildDockerImageTask.class)
        task.group = PubPlugin.PUB_GROUP
        task.description = 'Creates a Docker Image and tags it, using local docker commands (must be on PATH)'

        return task
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
            for(String repoKey : pubConfig.getDockerRepoKeys()) {
                doRun = true
                dockerLogin(pubConfig.username, pubConfig.password, repoKey)
            }
        }

        if(!doRun){
            logger.info("Skipping docker build")
        }
        else {
            logger.debug('docker build')
            cmd += " ."
            execute(cmd, null, "ERROR:")
            println(msg)
        }
    }
}
