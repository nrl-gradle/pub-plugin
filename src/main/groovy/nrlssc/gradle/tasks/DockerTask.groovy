package nrlssc.gradle.tasks

import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

import java.nio.charset.StandardCharsets

abstract class DockerTask extends DefaultTask {

    //TODO finish this multi-use capability
    @Internal
    List<String> getTags(){
        List<String> tags = new ArrayList<>()

        PubExtension pubExt = project.extensions.getByType(PubExtension.class)

        for(PubConfig pubConfig : pubExt.getPubConfigs())
        {
            for(String repoKey : pubConfig.getDockerRepoKeys())
            {
                String tagRoot = repoKey + '/' + project.group.toString().replaceAll(/\./, /\//) + '/' + project.getName()
                List<String> tagVers = new ArrayList<>()
                tags.add("$tagRoot:latest")
                tags.add("$tagRoot:${project.getVersion()}")
                if(pubExt.getExtraDockerTagVersions() != null){
                    for(String extra : pubExt.getExtraDockerTagVersions()){
                        tags.add("$tagRoot:$extra")
                    }
                }
            }

        }

        return tags
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



        if(p.waitFor() != 0 || (errorText != null && b.toString().trim().length() > 0 && b.toString().contains(errorText)))
        {
            throw new RuntimeException('Error in docker command execution: ' + b)
        }
        return txt
    }

    String dockerLogin(String user, String pass, String reg){
        logger.debug('docker login')
        return execute("docker login " +
                "-u ${user} " +
                "-p ${pass}" +
                "$reg", null,"Bad credentials")
    }
}
