package org.example

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.assertEquals

class WelcomeJobTest extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        // Register the shared library with the vars directory
        helper.registerSharedLibrary([
            name: 'pipeline-lib',
            retriever: helper.projectSource(),
            vars: ['welcomeJob']
        ])
        // Mock the echo step
        helper.registerAllowedMethod('echo', [String], { String msg ->
            helper.getCallStack().add([method: 'echo', args: [msg]])
        })
        // Bind the welcomeJob step to the test pipeline
        binding.setVariable('welcomeJob', { Map args = [:] ->
            def script = loadScript('vars/welcomeJob.groovy')
            script.call(args.name ?: 'User')
        })
    }

    @Test
    void testWelcomeJobWithDefaultName() {
        // Run the pipeline script calling welcomeJob
        runScript("""
            welcomeJob()
        """)

        // Verify the echo step was called with the correct message
        def callStack = helper.callStack.findAll { it.method == 'echo' }
        assertEquals(1, callStack.size())
        assertEquals('Welcome, User.', callStack[0].args[0])
    }

    @Test
    void testWelcomeJobWithCustomName() {
        // Run the pipeline script calling welcomeJob with a custom name
        runScript("""
            welcomeJob(name: 'Alice')
        """)

        // Verify the echo step was called with the correct message
        def callStack = helper.callStack.findAll { it.method == 'echo' }
        assertEquals(1, callStack.size())
        assertEquals('Welcome, Alice.', callStack[0].args[0])
    }
}