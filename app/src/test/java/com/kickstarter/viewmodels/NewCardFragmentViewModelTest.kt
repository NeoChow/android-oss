package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.CardFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.mock.services.MockStripe
import com.kickstarter.models.Project
import com.kickstarter.models.StoredCard
import com.kickstarter.ui.ArgumentsKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import type.PaymentTypes

class NewCardFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: NewCardFragmentViewModel.ViewModel
    private val allowedCardWarning = TestSubscriber<Int?>()
    private val allowedCardWarningIsVisible = TestSubscriber<Boolean>()
    private val appBarLayoutHasElevation = TestSubscriber<Boolean>()
    private val cardWidgetFocusDrawable = TestSubscriber<Int>()
    private val dividerIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<Void>()
    private val modalError = TestSubscriber<Void>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val reusableContainerIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<StoredCard>()

    private fun setUpEnvironment(environment: Environment, modal : Boolean = false, project : Project? = ProjectFactory.project()) {
        this.vm = NewCardFragmentViewModel.ViewModel(environment)
        this.vm.outputs.allowedCardWarning().map { it.first }.subscribe(this.allowedCardWarning)
        this.vm.outputs.allowedCardWarningIsVisible().subscribe(this.allowedCardWarningIsVisible)
        this.vm.outputs.appBarLayoutHasElevation().subscribe(this.appBarLayoutHasElevation)
        this.vm.outputs.cardWidgetFocusDrawable().subscribe(this.cardWidgetFocusDrawable)
        this.vm.outputs.dividerIsVisible().subscribe(this.dividerIsVisible)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.modalError().subscribe(this.modalError)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.reusableContainerIsVisible().subscribe(this.reusableContainerIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)

        if (modal) {
            val bundle = Bundle()
            bundle.putBoolean(ArgumentsKey.NEW_CARD_MODAL, true)
            bundle.putParcelable(ArgumentsKey.NEW_CARD_PROJECT, project)
            this.vm.arguments(bundle)
        } else {
            // The OS returns null if arguments aren't explicitly set
            this.vm.arguments(null)
        }
    }

    @Test
    fun testAllowedCardWarning() {
        setUpEnvironment(environment())

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarning.assertValue(null)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarning.assertValue(null)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarning.assertValues(null, R.string.Unsupported_card_type)
    }

    @Test
    fun testAllowedCardWarning_whenUSDProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment(), true, project = project)

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarning.assertValue(null)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarning.assertValue(null)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarning.assertValues(null, R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country)
    }

    @Test
    fun testAllowedCardWarning_whenNonUSDProject() {
        val project = ProjectFactory.mxProject()
        setUpEnvironment(environment(), true, project = project)

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarning.assertValue(R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarning.assertValues(R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country, null)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarning.assertValues(R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country, null,
                R.string.You_cant_use_this_credit_card_to_back_a_project_from_project_country)
    }

    @Test
    fun testAllowedCardWarningIsVisible() {
        setUpEnvironment(environment())

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarningIsVisible.assertValue(false)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarningIsVisible.assertValues(false)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarningIsVisible.assertValues(false, true)
    }

    @Test
    fun testAllowedCardWarningIsVisible_whenUSDProject() {
        setUpEnvironment(environment(), true)

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarningIsVisible.assertValue(false)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarningIsVisible.assertValues(false)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarningIsVisible.assertValues(false, true)
    }

    @Test
    fun testAllowedCardWarningIsVisible_whenNonUSDProject() {
        setUpEnvironment(environment(), true, ProjectFactory.mxProject())

        //Union Pay
        this.vm.inputs.cardNumber("620")
        this.allowedCardWarningIsVisible.assertValue(true)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.allowedCardWarningIsVisible.assertValues(true, false)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.allowedCardWarningIsVisible.assertValues(true, false, true)
    }

    @Test
    fun testAppBarLayoutHasElevation() {
        setUpEnvironment(environment())

        this.appBarLayoutHasElevation.assertValue(true)
    }

    @Test
    fun testAppBarLayoutHasElevation_whenModal() {
        setUpEnvironment(environment(), true)

        this.appBarLayoutHasElevation.assertValue(false)
    }

    @Test
    fun testCardWidgetFocusDrawable() {
        setUpEnvironment(environment())

        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        this.vm.inputs.cardFocus(true)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        //Union Pay
        this.vm.inputs.cardFocus(true)
        this.vm.inputs.cardNumber("620")
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_green_horizontal)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_red_400_horizontal)
    }

    @Test
    fun testCardWidgetFocusDrawable_whenUSDProject() {
        setUpEnvironment(environment(), true)

        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        this.vm.inputs.cardFocus(true)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        //Union Pay
        this.vm.inputs.cardFocus(true)
        this.vm.inputs.cardNumber("620")
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_green_horizontal)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_red_400_horizontal)

        //Remains red until error is resolved
        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_red_400_horizontal)
    }

    @Test
    fun testCardWidgetFocusDrawable_whenNonUSDProject() {
        setUpEnvironment(environment(), true, ProjectFactory.mxProject())

        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        this.vm.inputs.cardFocus(true)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_dark_grey_500_horizontal)

        //Union Pay
        this.vm.inputs.cardFocus(true)
        this.vm.inputs.cardNumber("620")
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal, R.drawable.divider_red_400_horizontal)

        //Visa
        this.vm.inputs.cardNumber("424")
        this.cardWidgetFocusDrawable.assertValuesAndClear(R.drawable.divider_green_horizontal)

        //Unknown
        this.vm.inputs.cardNumber("000")
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_red_400_horizontal)

        //Remains red until error is resolved
        this.vm.inputs.cardFocus(false)
        this.cardWidgetFocusDrawable.assertValue(R.drawable.divider_red_400_horizontal)
    }

    @Test
    fun testDividerIsVisible() {
        setUpEnvironment(environment())

        this.dividerIsVisible.assertValue(true)
    }

    @Test
    fun testDividerIsVisible_whenModal() {
        setUpEnvironment(environment(), true)

        this.dividerIsVisible.assertValue(false)
    }

    @Test
    fun testAPIError() {
        val apolloClient = object : MockApolloClient() {
            override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String, reusable: Boolean): Observable<StoredCard> {
                return Observable.error(Exception("oops"))
            }
        }
        val environment = environment().toBuilder().apolloClient(apolloClient).build()
        setUpEnvironment(environment)

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.cardNumber(CardFactory.card().number)
        this.vm.inputs.saveCardClicked()
        this.error.assertValueCount(1)
        this.koalaTest.assertValues("Viewed Add New Card", "Failed Payment Method Creation")
    }

    @Test
    fun testAPIError_whenModal() {
        val apolloClient = object : MockApolloClient() {
            override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String, reusable: Boolean): Observable<StoredCard> {
                return Observable.error(Exception("oops"))
            }
        }
        val environment = environment().toBuilder().apolloClient(apolloClient).build()
        setUpEnvironment(environment, true)

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.cardNumber(CardFactory.card().number)
        this.vm.inputs.saveCardClicked()
        this.modalError.assertValueCount(1)
        this.koalaTest.assertValues("Viewed Add New Card", "Failed Payment Method Creation")
    }

    @Test
    fun testStripeError() {
        val mockStripe = MockStripe(context(), true)
        val environment = environment().toBuilder().stripe(mockStripe).build()
        setUpEnvironment(environment)

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.cardNumber(CardFactory.card().number)
        this.vm.inputs.saveCardClicked()
        this.error.assertValueCount(1)
        this.modalError.assertNoValues()
    }

    @Test
    fun testStripeError_whenModal() {
        val mockStripe = MockStripe(context(), true)
        val environment = environment().toBuilder().stripe(mockStripe).build()
        setUpEnvironment(environment, true)

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        this.vm.inputs.card(CardFactory.card())
        this.vm.inputs.cardNumber(CardFactory.card().number)
        this.vm.inputs.saveCardClicked()
        this.error.assertNoValues()
        this.modalError.assertValueCount(1)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        val card = CardFactory.card()
        this.vm.inputs.card(card)
        this.vm.inputs.cardNumber(card.number)
        this.vm.inputs.saveCardClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testReusableContainerIsVisible() {
        setUpEnvironment(environment())

        this.reusableContainerIsVisible.assertValue(false)
    }

    @Test
    fun testReusableContainerIsVisible_whenModal() {
        setUpEnvironment(environment(), true)

        this.reusableContainerIsVisible.assertValue(true)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.name("Nathan Squid")
        val completeNumber = "4242424242424242"
        val incompleteNumber = "424242424242424"
        this.vm.inputs.card(CardFactory.card(completeNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(completeNumber)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(true)
        this.vm.inputs.card(CardFactory.card(incompleteNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(incompleteNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeNumber, null, 2020, "555"))
        this.vm.inputs.cardNumber(completeNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeNumber, 1, null, "555"))
        this.vm.inputs.cardNumber(completeNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeNumber, 1, 2020, null))
        this.vm.inputs.cardNumber(completeNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled_whenUSDProject() {
        setUpEnvironment(environment(), true)

        this.vm.inputs.name("Nathan Squid")
        val completeVisaNumber = "4242424242424242"
        val incompleteVisaNumber = "424242424242424"
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(true)
        this.vm.inputs.card(CardFactory.card(incompleteVisaNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(incompleteVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, null, 2020, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, null, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, 2020, null))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValuesAndClear(true, false)

        val completeDiscoverNumber = "6011111111111117"
        val incompleteDiscoverNumber = "601111111111111"
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(true)
        this.vm.inputs.card(CardFactory.card(incompleteDiscoverNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(incompleteDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, null, 2020, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, null, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, 2020, null))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled_whenNonUSDProject() {
        setUpEnvironment(environment(), true, ProjectFactory.mxProject())

        this.vm.inputs.name("Nathan Squid")
        val completeVisaNumber = "4242424242424242"
        val incompleteVisaNumber = "424242424242424"
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(true)
        this.vm.inputs.card(CardFactory.card(incompleteVisaNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(incompleteVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, null, 2020, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, null, "555"))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeVisaNumber, 1, 2020, null))
        this.vm.inputs.cardNumber(completeVisaNumber)
        this.saveButtonIsEnabled.assertValuesAndClear(true, false)

        val completeDiscoverNumber = "6011111111111117"
        val incompleteDiscoverNumber = "601111111111111"
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.vm.inputs.postalCode("11222")
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(incompleteDiscoverNumber, 1, 2020, "555"))
        this.vm.inputs.cardNumber(incompleteDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, null, 2020, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, null, "555"))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.card(CardFactory.card(completeDiscoverNumber, 1, 2020, null))
        this.vm.inputs.cardNumber(completeDiscoverNumber)
        this.saveButtonIsEnabled.assertValues(true, false)
    }

    @Test
    fun testSuccess() {
        val visa = StoredCardFactory.visa()
        val apolloClient = object : MockApolloClient() {
            override fun savePaymentMethod(paymentTypes: PaymentTypes, stripeToken: String, cardId: String, reusable: Boolean): Observable<StoredCard> {
                return Observable.just(visa)
            }
        }

        val environment = environment().toBuilder().apolloClient(apolloClient).build()
        setUpEnvironment(environment)

        this.vm.inputs.name("Nathan Squid")
        this.vm.inputs.postalCode("11222")
        val card = CardFactory.card()
        this.vm.inputs.card(card)
        this.vm.inputs.cardNumber(card.number)
        this.vm.inputs.saveCardClicked()
        this.success.assertValue(visa)
        this.koalaTest.assertValues("Viewed Add New Card", "Saved Payment Method")
    }
}
