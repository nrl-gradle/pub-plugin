package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction


/**
 * Created by scraft on 10/01/2024.
 */
class PublishDockerImageTask extends DockerTask {

    static PublishDockerImageTask createFor(Project project)
    {
        PublishDockerImageTask task = project.tasks.create("publishDockerImage", PublishDockerImageTask.class)
        task.group = PubPlugin.PUB_GROUP
        task.description = 'Creates a Docker Image and publishes it, using local docker commands (must be on PATH)'

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
                String tagRoot = repoKey + '/' + project.group.toString().replaceAll(/\./, /\//) + '/' + project.getName() + ':'
                List<String> tagVers = new ArrayList<>()
                tagVers.add('latest')
                tagVers.add(project.getVersion() + '')
                tagVers.addAll(pubExt.getExtraDockerTagVersions())

                if(pubConfig.username == null || pubConfig.username.length() == 0 ||
                pubConfig.password == null || pubConfig.password.length() == 0){
                    logger.error("Cannot push to docker registry (" + repoKey + ") without valid credentials")
                    return
                }

                dockerLogin(pubConfig.username, pubConfig.password, repoKey)


                String msg = "Successfully pushed docker images to registry for " + project.getName() + " with tags:\n"
                for(String tagVer : tagVers) {
                    logger.debug('docker push ')
                    execute("docker push $tagRoot$tagVer", null, "Error response from daemon")
                    msg += "    $tagRoot$tagVer\n"
                }

                println(msg)

            }

        }
    }


}
