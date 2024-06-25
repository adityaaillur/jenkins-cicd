import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.util.Secret


def getEnvironmentVariable(String name) {
    def value = ""
    def envFile = new File('/etc/environment')
    envFile.eachLine { line ->
        if (line.startsWith("${name}=")) {
            value = line.split('=')[1].replaceAll(/^"|"$/, '')
        }
    }
    return value
}

def jenkinsInstance = Jenkins.getInstance()
def domain = Domain.global()
def store = jenkinsInstance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def dockerHubUsername = getEnvironmentVariable('DH_USERNAME')
def dockerHubPassword = getEnvironmentVariable('DH_TOKEN')
def gitHubUsername = getEnvironmentVariable('GH_USERNAME')
def gitHubPassword = getEnvironmentVariable('GH_TOKEN')

def credentials = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "dockerhub", 
    "Docker Hub Credentials",
    dockerHubUsername,
    dockerHubPassword
)

def credentials2 = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github", 
    "GitHub Credentials",
    gitHubUsername,
    gitHubPassword
)
def credentials3 = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "github-token", 
    "GitHub Token",
    Secret.fromString(gitHubPassword)
)
store.addCredentials(domain, credentials)
store.addCredentials(domain, credentials2)
store.addCredentials(domain, credentials3)
println("Credentials added successfully")
jenkinsInstance.save()
