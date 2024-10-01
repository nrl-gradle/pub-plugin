package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import nrlssc.gradle.helpers.PluginUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

/**
 * Created by scraft on 10/01/2024.
 */
class PublishDockerImageTask extends DefaultTask{

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
                String latestTag = repoKey + '/' + project.group + '/' + project.getName() + ':latest'
                String verTag = repoKey + '/' + project.group + '/' + project.getName() + ':' + project.getVersion()


                logger.debug('docker login')
                execute("docker login " +
                        "-u ${pubConfig.username} " +
                        "--password-stdin " +
                        "$repoKey", pubConfig.password, true)

                logger.debug('docker build')
                execute("docker build . --tag $latestTag --tag $verTag ")
                logger.debug('docker push ')
                execute("docker push $verTag")
                println("Successfully published $verTag to Docker Registry")


                //println(p.getText().trim())


                if(pubExt.publishType.equalsIgnoreCase('release'))
                {
                    logger.debug('docker push')
                    execute("docker push $latestTag")
                    println("Successfully published $latestTag to Docker Registry")
                }


            }

        }
    }

    String execute(String cmd, String sendToStdin = null, boolean breakOnError = false){
        def env = System.getenv().collect { k, v -> "$k=$v" }

        Process p = cmd.execute(env, project.projectDir)
        if(sendToStdin != null) {
            sendToStdin = sendToStdin + "\n"
            p.getOut().write(sendToStdin.getBytes(StandardCharsets.UTF_8))
            p.getOut().close()
        }
        def b = new StringBuffer()
        p.consumeProcessErrorStream(b)

        String txt = p.text

        if(breakOnError && b.toString().trim().length() > 0)
        {
            throw new RuntimeException('Error in Docker Publish: ' + b)
        }
        return txt + b.toString().trim()
    }
}
