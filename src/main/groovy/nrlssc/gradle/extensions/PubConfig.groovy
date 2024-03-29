package nrlssc.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.credentials.Credentials
import org.gradle.util.ConfigureUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PubConfig {
    private static Logger logger = LoggerFactory.getLogger(PubConfig.class)

    String name
    String url
    String pattern
    String username
    String password
    Class credentialsClass
    Closure credentials
    Closure authentication

    static String defaultPattern = "{url}/{key}"

    //region configure repos
    private List<RepoConfig> relRepos = new ArrayList<>()
    private List<RepoConfig> snapRepos = new ArrayList<>()
    private List<String> yumRepoKeys = new ArrayList<>()

    PubConfig()
    {
        this.pattern = defaultPattern
    }
    
    List<String> getYumRepoKeys()
    {
        return yumRepoKeys
    }

    protected List<RepoConfig> getRelRepos(){
        return relRepos
    }

    protected List<RepoConfig> getSnapRepos(){
        return snapRepos
    }

    void release(String key){
        RepoConfig rc = new RepoConfig(key)
        relRepos.add(rc)
    }
    
    void release(Closure repoClosure)
    {
        release(ConfigureUtil.configure(repoClosure, new RepoConfig()))
    }
    
    void release(RepoConfig repoConfig)
    {
        relRepos.add(repoConfig)
    }

    void snapshot(String key){
        RepoConfig rc = new RepoConfig(key)
        snapshot(rc)
    }

    void snapshot(Closure repoClosure)
    {
        snapshot(ConfigureUtil.configure(repoClosure, new RepoConfig()))
    }

    void snapshot(RepoConfig repoConfig)
    {
        snapRepos.add(repoConfig)
    }
    
    void yum(String key)
    {
        yumRepoKeys.add(key)
    }

    void yum(RepoConfig repoConfig)
    {
        yumRepoKeys.add(repoConfig.key)
    }
    
    void yum(Closure yumClosure)
    {
        yum(ConfigureUtil.configure(yumClosure, new RepoConfig()))
    }

    boolean isInsecure()
    {
        return url.startsWith("http://")
    }

    void credentials(Class<? extends Credentials> credentialsClass, Closure credentials)
    {
        this.credentialsClass = credentialsClass
        this.credentials = credentials
    }

    void authentication(Closure closure)
    {
        this.authentication = closure
    }
    //endregion configure repos
    
    
   
}
