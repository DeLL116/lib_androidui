package com.nochino.support.androidui.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.nochino.support.androidui.R
import com.nochino.support.androidui.testing.CountingIdlingResourceViewModelFactory
import com.nochino.support.networking.presenter.LoadingResourcePresenter
import com.nochino.support.networking.presenter.LoadingResourcePresenterView
import com.nochino.support.networking.vo.*
import timber.log.Timber

open class BaseObserverErrorFragment<D, VM : LoadingResourceViewModel<D>> :
    BaseErrorFragment(),
    Observer<LoadingResource<D>>,
    LoadingResourcePresenterView<D>,
    LoadingResourceViewModelCreator<D, VM> {

    private var loadingResourcePresenter: LoadingResourcePresenter<D> = LoadingResourcePresenter(this)

    @Suppress("UNCHECKED_CAST")
    override fun createViewModelClass(arguments: Bundle?): Class<VM>? {
        return LoadingResourceViewModelCreator.createFromBundle(arguments)
    }

    override val loadingResourceViewModelClass: Class<VM>? by lazy {
        createViewModelClass(arguments)
    }

    companion object {
        // TODO: Doc
        @JvmStatic
        fun <D, VM: LoadingResourceViewModel<D>> newInstance(loadingResourceViewModel: LoadingResourceViewModel<D>) =
            BaseObserverErrorFragment<D, VM>().apply {
                arguments = Bundle().apply {
                    putString(ARG_VIEW_MODEL_CLASS_NAME, loadingResourceViewModel.javaClass.canonicalName)
                }
            }
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
        view.post {
            // Clear the scroll-flags on the ToolBar so it no longer tries to
            // scroll in the direction of the user swipe / fling
            setAppBarScrollFlags(0)
        }
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
                .getFragmentViewModel(this@BaseObserverErrorFragment)
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
    }

    override fun showError(loadingResource: LoadingResource<D>) {
        mDrawable = resources.getDrawable(R.drawable.ic_sad_cloud, null)
        mMessage = (loadingResource.message.toString())
        setDefaultBackground(true)

        mButtonText = (resources.getString(R.string.error_fragment_retry))
        mButtonClickListener = (View.OnClickListener {
            this@BaseObserverErrorFragment.loadingResourceViewModel?.fetchLiveData()?.observe(this, this)
        })
    }

    override fun showSuccess(loadingResource: LoadingResource<D>) {
        // Have the fragment remove itself. The Fragment that created
        // this ErrorFragment should have received it's callback for the data.
        requireFragmentManager().beginTransaction().remove(this).commit()
    }

    override fun showLoading(loadingResource: LoadingResource<D>) {

    }
}