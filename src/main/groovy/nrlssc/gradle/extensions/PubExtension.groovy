package nrlssc.gradle.extensions


import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PubExtension {
    private static Logger logger = LoggerFactory.getLogger(PubExtension.class)
    private Project project
    
    String publishType = 'release' //'release' or 'snapshot'
    
    PubExtension(Project proj)
    {
        this.project = proj
    }
    
    List<PubConfig> pubConfigs = new ArrayList<>()
    void repo(Closure pubClosure)
    {
        repo(ConfigureUtil.configure(pubClosure, new PubConfig()))
    }
    void repo(PubConfig pubConfig)
    {
        pubConfigs.add(pubConfig)
    }
    
    List<String> getYumRepoURLs
    
    void selectRepos() {
        for(PubConfig pbcf : pubConfigs){
            
            List<RepoConfig> repoConfigs
            switch (publishType){
                case 'snapshot':
                    repoConfigs = pbcf.getSnapRepos()
                    break
                default:
                    repoConfigs = pbcf.getRelRepos()
                    break
            }
            
            def nam = pbcf.getName() == null ? "Default" : pbcf.getName()

            project.publishing.repositories {
                for(RepoConfig rc : repoConfigs)
                {
                    
                    def key = rc.key
                    def keyURL = String.join("/", pbcf.getUrl(), rc.key)

                    if(rc.maven) {
                        maven{
                            name = "$nam-$key"
                            url = keyURL
                            if(pbcf.username != null && pbcf.password != null)
                            {
                                credentials{
                                    username = pbcf.username
                                    password = pbcf.password
                                }
                            }
                        }
                    }
                    else{
                        ivy{
                            name = "$nam-$key"
                            url = keyURL

                            if(pbcf.username != null && pbcf.password != null)
                            {
                                credentials{
                                    username = pbcf.username
                                    password = pbcf.password
                                }
                            }
                            if(rc.patternLayout != null)
                            {
                                patternLayout(rc.patternLayout)
                            }
                        }
                    }
                }
            }
            
        }
    }
    
}
