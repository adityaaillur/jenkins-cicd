import jenkins.model.Jenkins
import jenkins.branch.BranchSource
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource
import org.jenkinsci.plugins.github_branch_source.BranchDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.OriginPullRequestDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait
import org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait.TrustContributors
import jenkins.scm.api.trait.SCMTrait
import java.util.Arrays

def repositories = [
    [owner: 'cyse7125-su24-team15', name: 'ami-jenkins'],
    [owner: 'cyse7125-su24-team15', name: 'infra-jenkins'],
    [owner: 'cyse7125-su24-team15', name: 'static-site'],
    [owner: 'cyse7125-su24-team15', name: 'k8s-yaml-manifests'],
    [owner: 'cyse7125-su24-team15', name: 'webapp-cve-processor'],
    [owner: 'cyse7125-su24-team15', name: 'database-migration'],
    [owner: 'cyse7125-su24-team15', name: 'helm-webapp-cve-processor'],
    [owner: 'cyse7125-su24-team15', name: 'infra-aws'],
    [owner: 'cyse7125-su24-team15', name: 'webapp-cve-consumer'],
]

def githubCredentialsId = 'github'

repositories.each { repo ->
    def repoOwner = repo.owner
    def repoName = repo.name
    def jobName = "${repoOwner}-${repoName}-pipeline"

    def jenkinsInstance = Jenkins.getInstance()

    def job = jenkinsInstance.getItem(jobName)
    if (job == null) {
        job = jenkinsInstance.createProject(WorkflowMultiBranchProject, jobName)
        println "Multibranch Pipeline job '${jobName}' created successfully"
    } else {
        println "Multibranch Pipeline job '${jobName}' already exists"
    }

    def scmSource = new GitHubSCMSource(repoOwner, repoName)
    scmSource.setCredentialsId(githubCredentialsId)

    scmSource.setTraits(Arrays.asList(
        new BranchDiscoveryTrait(3),
        new OriginPullRequestDiscoveryTrait(2),
        new ForkPullRequestDiscoveryTrait(1, new TrustContributors())
    ))

    def branchSource = new BranchSource(scmSource)
    job.getSourcesList().clear()
    job.getSourcesList().add(branchSource)

    job.getProjectFactory().setScriptPath('Jenkinsfile')  

    job.save()

    println "Multibranch Pipeline job '${jobName}' configured successfull"
}
