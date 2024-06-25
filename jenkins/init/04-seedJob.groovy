import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import javaposse.jobdsl.plugin.ExecuteDslScripts
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def instance = Jenkins.getInstance()
if (instance == null) {
    throw new IllegalStateException("Jenkins instance is not available")
}

def jobName = 'seed-job'
def seedJob = instance.getItem(jobName)

if (seedJob == null) {
    try {
        seedJob = instance.createProject(FreeStyleProject.class, jobName)

        def builder = new ExecuteDslScripts()
        builder.setTargets('seed.groovy')
        builder.setUseScriptText(false)

        seedJob.getBuildersList().add(builder)
        seedJob.save()

        def queue = instance.getQueue()
        queue.schedule(seedJob, 0)

        Thread.start {
            sleep(30000) 

            ScriptApproval scriptApproval = ScriptApproval.get()
            int maxRetries = 12 

            for (int i = 0; i < maxRetries; i++) {
                if (!scriptApproval.pendingScripts.isEmpty()) {
                    scriptApproval.pendingScripts.each {
                        scriptApproval.approveScript(it.hash)
                    }
                    println "Approved pending scripts"
                    break
                } else {
                    println "Waiting"
                    sleep(5000)
                }
            }

            if (scriptApproval.pendingScripts.isEmpty()) {
                println "No pending scripts"
            }
        }

    } catch (Exception e) {
        println "An error occurred: ${e.message}"
        e.printStackTrace()
    }
} else {
    println "Seed job already exists: ${jobName}"
}
