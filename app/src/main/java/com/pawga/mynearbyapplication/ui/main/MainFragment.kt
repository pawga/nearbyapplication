package com.pawga.mynearbyapplication.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.material.snackbar.Snackbar
import com.pawga.mynearbyapplication.R
import com.pawga.mynearbyapplication.databinding.MainFragmentBinding
import com.pawga.mynearbyapplication.domain.State
import com.pawga.mynearbyapplication.extensions.exhaustive
import timber.log.Timber


class MainFragment : Fragment(), MainView {

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1001

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    // Our handle to Nearby Connections
    private var connectionsClient: ConnectionsClient? = null
    private var packageName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        activity?.also {
            connectionsClient = Nearby.getConnectionsClient(it)
            packageName = it.packageName
        }

        viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(connectionsClient, packageName)
        ).get(MainViewModel::class.java)

        viewModel.stateLiveData.observe(viewLifecycleOwner, {
            render(it)
        })

        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.opponentMessages.adapter = ListAdapter(viewModel.receivedMessages)
        binding.opponentMessages.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)
        binding.ownerMessages.adapter = ListAdapter(viewModel.sentMessages)
        binding.ownerMessages.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.VERTICAL, false)

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return
        }
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                viewModel.setStatus(State.NonPermissions)
                return
            }
        }
    }

    override fun render(state: State) {
        when (state) {
            is State.RequiredPermissions -> {
                renderPermissionsState()
            }
            is State.Finding -> {
                Timber.d("State.Finding")
                reset()
            }
            is State.RequiredOpponent -> {
                Timber.d("State.RequiredOpponent")
                reset()
            }
            is State.NonPermissions -> {
                Snackbar.make(
                    requireView(),
                    getString(R.string.error_missing_permissions),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(getString(R.string.cancel), null).show()
            }
            is State.Error -> {
                Timber.d("State.ErrorState")
            }
            is State.Connected -> {
                Timber.d("State.Ready")
            }
        }.exhaustive
    }

    private fun reset() {
        val context = context ?: return
        hideKeyboardFrom(context, binding.message)
    }

    private fun renderPermissionsState() {
        context?.also {
            if (!hasPermissions(it, *REQUIRED_PERMISSIONS)) {
                requestPermissions(
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_REQUIRED_PERMISSIONS
                )
            } else {
                viewModel.setStatus(State.RequiredOpponent)
            }
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}