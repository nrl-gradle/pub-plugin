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
                String tagRoot = repoKey + '/' + project.group + '/' + project.getName() + ':'
                List<String> tagVers = new ArrayList<>()
                tagVers.add('latest')
                tagVers.add(project.getVersion() + '')
                tagVers.addAll(pubExt.getExtraDockerTagVersions())


                String msg = "Successfully built docker image for " + project.getName() + " with tags:\n"

                String cmd = "docker build . "
                for(String tagVer : tagVers)
                {
                    cmd = cmd + "--tag $tagRoot$tagVer "
                    msg += "    $tagRoot$tagVer\n"
                }

                logger.debug('docker build')
                execute(cmd)

                println(msg)

            }

        }
    }
}
