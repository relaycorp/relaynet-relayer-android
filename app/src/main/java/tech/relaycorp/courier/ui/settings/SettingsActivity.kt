package tech.relaycorp.courier.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.stationhead.android.shared.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.activity_settings.deleteData
import kotlinx.android.synthetic.main.activity_settings.storageAvailable
import kotlinx.android.synthetic.main.activity_settings.storageMaxSeek
import kotlinx.android.synthetic.main.activity_settings.storageMaxValue
import kotlinx.android.synthetic.main.activity_settings.storageTotal
import kotlinx.android.synthetic.main.activity_settings.storageUsed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import tech.relaycorp.courier.R
import tech.relaycorp.courier.ui.BaseActivity
import tech.relaycorp.courier.ui.common.format
import tech.relaycorp.courier.ui.common.set
import javax.inject.Inject

class SettingsActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory<SettingsViewModel>

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(SettingsViewModel::class.java)
    }

    private var deleteDataDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        setContentView(R.layout.activity_settings)
        setupNavigation(R.drawable.ic_close)

        deleteData.setOnClickListener { openDeleteDataDialog() }

        viewModel
            .deleteDataEnabled()
            .onEach { deleteData.set(it) }
            .launchIn(lifecycleScope)

        viewModel
            .storageStats()
            .onEach {
                storageUsed.text = getString(
                    R.string.settings_used_storage,
                    it.used.format(this),
                    it.usedPercentage
                )
                storageAvailable.text =
                    getString(R.string.settings_available_storage, it.available.format(this))
                storageTotal.text =
                    getString(R.string.settings_total_storage, it.total.format(this))
            }
            .launchIn(lifecycleScope)

        viewModel
            .maxStorageBoundary()
            .onEach { boundary ->
                StorageSizeSeekBarHelper(boundary, storageMaxSeek)
                    .onChangeListener = viewModel::maxStorageChanged
            }
            .launchIn(lifecycleScope)

        combine(
            viewModel.maxStorage(),
            viewModel.maxStorageBoundary()
        ) { value, boundary ->
            ((value - boundary.min) / boundary.step).bytes.toInt()
        }
            .take(1)
            .onEach { storageMaxSeek.progress = it }
            .launchIn(lifecycleScope)

        viewModel
            .maxStorage()
            .onEach { storageMaxValue.text = it.format(this) }
            .launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteDataDialog?.dismiss()
        deleteDataDialog = null
    }

    private fun openDeleteDataDialog() {
        deleteDataDialog = AlertDialog.Builder(this)
            .setTitle(R.string.settings_delete_dialog_title)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteDataClicked() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}