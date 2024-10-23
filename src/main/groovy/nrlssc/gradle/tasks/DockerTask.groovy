package nrlssc.gradle.tasks

import org.gradle.api.DefaultTask

import java.nio.charset.StandardCharsets

abstract class DockerTask extends DefaultTask {


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
