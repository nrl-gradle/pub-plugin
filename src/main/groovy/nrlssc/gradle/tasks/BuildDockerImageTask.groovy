package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
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
    void run()
    {
        PubExtension pubExt = project.extensions.getByType(PubExtension.class)

        for(PubConfig pubConfig : pubExt.getPubConfigs())
        {
            for(String repoKey : pubConfig.getDockerRepoKeys())
            {
                String latestTag = repoKey + '/' + project.group + '/' + project.getName() + ':latest'
                String verTag = repoKey + '/' + project.group + '/' + project.getName() + ':' + project.getVersion()

                logger.debug('docker build')
                execute("docker build . --tag $latestTag --tag $verTag ")
                println("Successfully built $verTag ")

            }

        }
    }
}
