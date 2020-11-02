package nrlssc.gradle.tasks

import nrlssc.gradle.PubPlugin
import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets




class PublishYumTask extends DefaultTask {

    static PublishYumTask createFor(Project project)
    {
        PublishYumTask pubYum = project.tasks.create("publishYum", PublishYumTask.class)
        pubYum.group = PubPlugin.PUB_GROUP
        pubYum.description = 'Publish yumArtifact files to all yum repos'
        return pubYum
    }
    
    @InputFiles
    public List<File> getInputFiles()
    {
        Project project = getProject()
        List<File> inputFiles = new ArrayList<>()
        project.configurations.getByName(PubPlugin.YUM_CONFIG).artifacts.each{art ->
            inputFiles.add(art.file)
        }
        return inputFiles
    }

    @TaskAction
    void run()
    {
        Project project = getProject()

        PubExtension pubExt = project.extensions.getByType(PubExtension.class)
        
        for(PubConfig pubConfig : pubExt.getPubConfigs())
        {
            for(String repoKey : pubConfig.getYumRepoKeys())
            {
                for(File f : getInputFiles()) {
                    URL url = new URL(pubConfig.url + "/" + repoKey + "/" + f.getName())
                    

                    String auth = pubConfig.username + ":" + pubConfig.password

                    byte[] encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8))
                    String authHeaderValue = "Basic " + new String(encodedAuth)

                    HttpURLConnection httpCon = url.openConnection()

                    httpCon.setRequestProperty("Authorization", authHeaderValue)    


                    httpCon.setDoOutput(true)
                    httpCon.setRequestMethod("PUT")
                    
                    OutputStream out = httpCon.getOutputStream()
                    out.write(f.getBytes())
                    out.close()
                    httpCon.getInputStream()
                     
                }
            }

        }
        
        
    }
}
