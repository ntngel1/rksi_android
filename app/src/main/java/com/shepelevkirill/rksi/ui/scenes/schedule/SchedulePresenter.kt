package com.shepelevkirill.rksi.ui.scenes.schedule

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shepelevkirill.rksi.App
import com.shepelevkirill.rksi.data.core.models.ScheduleModel
import com.shepelevkirill.rksi.data.core.models.SubjectModel
import com.shepelevkirill.rksi.data.core.repository.ScheduleRepository
import com.shepelevkirill.rksi.ui.adapters.ScheduleAdapter
import com.shepelevkirill.rksi.utils.processors.DateProcessor
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

@InjectViewState
class SchedulePresenter : MvpPresenter<ScheduleMvpView>() {
    @Inject lateinit var scheduleRepository: ScheduleRepository

    private var scheduleLoader: Disposable? = null

    init {
        App.appComponent.inject(this)
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadSchedule()
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduleLoader?.dispose()
    }

    fun onScrolled(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val adapter = recyclerView.adapter as ScheduleAdapter

        val lastVisibleItemPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVisibleItem = adapter.get(lastVisibleItemPos)

        when(lastVisibleItem) {
            is SubjectModel -> {
                val title = DateProcessor.getDate(lastVisibleItem.date)
                viewState.setTitle(title)
            }
            is LocalDate -> {
                val title = DateProcessor.getDate(lastVisibleItem)
                viewState.setTitle(title)
            }
            else -> throw NoSuchElementException("Can't associate this with element")
        }
    }

    fun onRefresh() {
        loadSchedule()
    }

    private fun loadSchedule() {
        if (scheduleLoader != null)
            return

        viewState.clearSchedule()
        scheduleRepository.getScheduleForGroup("ПОКС-11")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: SingleObserver<List<ScheduleModel>> {
                override fun onSubscribe(d: Disposable) {
                    scheduleLoader = d
                }

                override fun onSuccess(t: List<ScheduleModel>) {
                    viewState.showSchedule(t)

                    scheduleLoader!!.dispose()
                    scheduleLoader = null
                }

                override fun onError(e: Throwable) {
                    viewState.showError()
                }

            })
    }
}