package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import org.junit.Test
import rx.observers.TestSubscriber

class BackingFragmentViewModelTest :  KSRobolectricTestCase() {
    private lateinit var vm: BackingFragmentViewModel.ViewModel

    private val project = TestSubscriber.create<Project>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingFragmentViewModel.ViewModel(environment)
        this.vm.outputs.project().subscribe(this.project)
    }

    @Test
    fun testProject() {
        val project = ProjectFactory.backedProject()
        setUpEnvironment(environment())

        this.vm.inputs.project(project)
        this.project.assertValue(project)
    }

}
