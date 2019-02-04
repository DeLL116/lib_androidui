package com.nochino.support.androidui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.Factory
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelFactory
import com.nochino.support.networking.presenter.LoadingResourcePresenter
import com.nochino.support.networking.presenter.LoadingResourcePresenterView
import com.nochino.support.networking.vo.LoadingResource
import com.nochino.support.networking.vo.LoadingResourceViewModel
import com.nochino.support.networking.vo.LoadingResourceViewModelCreator
import com.nochino.support.networking.vo.LoadingStatus
import timber.log.Timber

/**
 * A subclass of [BaseFragment] that can fetch and observe [LoadingResource] [LiveData] as well as
 * provide error handling when fetching the data fails.
 *
 * Subclasses are required to provide means for this fragment to create the [loadingResourceViewModel]
 * instance. They have a few options to doing so :
 *
 *  - Override the [loadingResourceViewModelClass] property and return the *Class* object of
 * the [LoadingResourceViewModel] being observed.
 *
 * - Provide the canonical class name of the [LoadingResourceViewModel] subclass to the fragment's
 * bundle arguments via the ARG_VIEW_MODEL_CLASS_NAME constant key from [LoadingResourceViewModelCreator]
 *
 * - Override [initLoadingResourceViewModel] and set the value on the [loadingResourceViewModel] instance.
 *  This is especially useful if the associated ViewModel needs a custom [ViewModelProvider] [Factory]
 *
 * This fragment also comes with built-in error handling when fetching the
 * [LoadingResource] fails, and displays the associated error message
 * (along with recovery efforts) in a [BaseObserverErrorFragment]. Subclasses
 * can provide their own error handling efforts by overriding [showError].
 *
 * Subclasses must provide the data class being observed as type [D], as well as the
 * corresponding [LoadingResourceViewModel] class as type [VM]. Note, the [LoadingResourceViewModel] class
 * provided must observe the same data object provided as [D]. This is enforced by
 * the class signature "where VM: LoadingResourceViewModel<D>" and ensures the type class
 * provided as the Data is also the data type provided to the subclass of [LoadingResourceViewModel]
 *
 * @param D The Data class being observed
 * @param VM The [LoadingResourceViewModel] Class, with the observed type being that defined by [D]
 */
abstract class BaseObserverFragment<D, VM : LoadingResourceViewModel<D>> :
    BaseFragment(),
    Observer<LoadingResource<D>>,
    LoadingResourcePresenterView<D>,
    LoadingResourceViewModelCreator<D, VM> {

    lateinit var loadingResourcePresenter: LoadingResourcePresenter<D>

    override fun createViewModelClass(arguments: Bundle?): Class<VM>? {
        return LoadingResourceViewModelCreator.createFromBundle(arguments)
    }

    override val loadingResourceViewModelClass: Class<VM>? by lazy {
        createViewModelClass(arguments)
    }

    override var loadingResourceViewModel: LoadingResourceViewModel<D>? = null
        set(value) {
            if (field == null) {
                field = value
            } else {
                // The ViewModel should not be changed after it's first
                // instantiated as this may cause confusion in implementing
                // classes.
                throw IllegalStateException("View Model has already been set. It should not be changed!")
            }
        }

    /**
     * Creates the [loadingResourceViewModel] instance containing the [LoadingResource] [LiveData]
     * that will be fetched and observed by this fragment.
     */
    override fun initLoadingResourceViewModel(loadingResourceViewModelClass: Class<VM>?) {
        loadingResourceViewModelClass?.let {
            loadingResourceViewModel = ViewModelProviders.of(requireActivity()).get(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLoadingResourceViewModel(loadingResourceViewModelClass)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingResourcePresenter = LoadingResourcePresenter(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set the observer on ViewModel's LiveData. This Observer will be
        // notified when the underlying data in the ViewModel has changed.
        // Note...currently this must be fetched in onActivityCreated for
        // some tests to pass (CountingIdlingResource increment/decrement)
        loadingResourceViewModel?.fetchLiveData()?.apply {
            CountingIdlingResourceViewModelFactory
                .getFragmentViewModel(this@BaseObserverFragment)
                .incrementTestIdleResourceCounter()
        }?.observe(this, this)
    }

    @CallSuper
    override fun onChanged(t: LoadingResource<D>?) {
        // Log the change
        Timber.d("%s :: LoadingResource in [%s] of data type [%s] changed with status [%s]",
            "onChanged()",
            javaClass.simpleName,
            getGenericTypeClassName(0),
            t?.loadingStatus ?: "NoData" // Elvis to log "NoData" if no data can be found
        )

        // Invoke the appropriate presenter method
        when {
            t?.loadingStatus == LoadingStatus.LOADING -> loadingResourcePresenter.onLoading(t)
            t?.loadingStatus == LoadingStatus.SUCCESS -> loadingResourcePresenter.onSuccess(t)
            t?.loadingStatus == LoadingStatus.ERROR -> loadingResourcePresenter.onError(t)
        }

        CountingIdlingResourceViewModelFactory
            .getFragmentViewModel(this)
            .decrementIdleResourceCounter()
    }

    override fun showError(loadingResource: LoadingResource<D>) {
        loadingResourceViewModel?.let {
            childFragmentManager
                .beginTransaction()
                .add(view!!.id, BaseObserverErrorFragment.newInstance<D, VM>(it))
                .commit()
        }
    }

    override fun showSuccess(loadingResource: LoadingResource<D>) {
        /* No current default op */
        /* Subclasses to provide operations upon success */
    }

    override fun showLoading(loadingResource: LoadingResource<D>) {
        /* No current default op */
    }
}