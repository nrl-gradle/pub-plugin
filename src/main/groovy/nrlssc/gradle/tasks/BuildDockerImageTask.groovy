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
class BuildDockerImageTask extends DefaultTask{

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

    String execute(String cmd, String sendToStdin = null, String errorText = null){
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

        if(errorText != null && b.toString().trim().length() > 0 && b.toString().contains(errorText))
        {
            throw new RuntimeException('Error in Docker Publish: ' + b)
        }
        return txt + b.toString().trim()
    }
}
