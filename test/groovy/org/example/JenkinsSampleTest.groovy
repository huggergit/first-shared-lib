package org.example

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.assertEquals
import static groovy.test.GroovyAssert.assertTrue

class JenkinsSampleTest extends DeclarativePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        // Register the shared library with the vars directory
        helper.registerSharedLibrary([
            name: 'pipeline-lib',
            retriever: helper.projectSource(),
            vars: ['jenkinsSample']
        ])
        // Mock pipeline steps
        helper.registerAllowedMethod('sh', [String], { String cmd ->
            helper.getCallStack().add([method: 'sh', args: [cmd]])
            return null
        })
        helper.registerAllowedMethod('git', [Map], { Map args ->
            helper.getCallStack().add([method: 'git', args: [args]])
        })
        // Mock pipeline constructs
        helper.registerAllowedMethod('pipeline', [Closure], null)
        helper.registerAllowedMethod('stage', [String, Closure], null)
        helper.registerAllowedMethod('steps', [Closure], null)
        helper.registerAllowedMethod('agent', [Map], null)
        // Bind the jenkinsSample step to the test pipeline
        binding.setVariable('jenkinsSample', { String repoUrl ->
            def script = loadScript('vars/jenkinsSample.groovy')
            script.call(repoUrl)
        })
    }

    @Test
    void testPipelineExecution() {
        // Run the pipeline script calling jenkinsSample
        runScript("""
            jenkinsSample('https://github.com/example/repo.git')
        """)

        // Verify job status
        assertJobStatusSuccess()

        // Verify the call stack for expected steps
        def callStack = helper.callStack

        // Check sh commands
        def shCalls = callStack.findAll { it.method == 'sh' }
        assertEquals(5, shCalls.size())
        assertEquals('mvn --version', shCalls[0].args[0])
        assertEquals('java -version', shCalls[1].args[0])
        assertEquals('mvn clean', shCalls[2].args[0])
        assertEquals('mvn test', shCalls[3].args[0])
        assertEquals('mvn package -DskipTests', shCalls[4].args[0])

        // Check git step
        def gitCalls = callStack.findAll { it.method == 'git' }
        assertEquals(1, gitCalls.size())
        assertEquals('https://github.com/example/repo.git', gitCalls[0].args[0].url)
        assertEquals('master', gitCalls[0].args[0].branch)
    }

    @Test
    void testStagePresence() {
        // Run the pipeline script calling jenkinsSample
        runScript("""
            jenkinsSample('https://github.com/example/repo.git')
        """)

        // Verify stage names
        def stageNames = helper.callStack.findAll { it.method == 'stage' }.collect { it.args[0] }
        assertEquals(['Tools initialization', 'Checkout Code', 'Cleaning workspace', 'Running Testcase', 'Packing Application'], stageNames)
    }
}