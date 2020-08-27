package co.appreactor.nextcloud.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.appreactor.nextcloud.news.db.NewsItem
import com.nextcloud.android.sso.exceptions.SSOException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.ui.UiExceptionManager
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class NewsFragment : Fragment() {

    private val model: NewsFragmentModel by viewModel()

    private val itemsAdapter = ItemsAdapter(
        items = mutableListOf(),
        feeds = mutableListOf()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_news,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.apply {
            inflateMenu(R.menu.menu_news)

            lifecycleScope.launchWhenResumed {
                model.showReadNews.collect { show ->
                    val item = menu.findItem(R.id.showReadNews)

                    if (show) {
                        item.setIcon(R.drawable.ic_baseline_visibility_24)
                        item.setTitle(R.string.hide_read_news)
                    } else {
                        item.setIcon(R.drawable.ic_baseline_visibility_off_24)
                        item.setTitle(R.string.show_read_news)
                    }
                }
            }

            setOnMenuItemClickListener {
                if (it.itemId == R.id.showReadNews) {
                    model.showReadNews.value = !model.showReadNews.value
                    return@setOnMenuItemClickListener true
                }

                false
            }
        }

        lifecycleScope.launch {
            whenResumed {
                showAuthOrShowData()
            }
        }
    }

    private suspend fun showAuthOrShowData() {
        try {
            SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            model.sync()
            showData()
        } catch (e: SSOException) {
            UiExceptionManager.showDialogForException(context, e)
        }
    }

    private suspend fun showData() {
        progress.isVisible = true

        itemsView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = itemsAdapter
        }

        model.getNewsAndFeeds().collect {
            if (it.first.isNotEmpty()) {
                progress.isVisible = false
            }

            val onItemClick: (NewsItem) -> Unit = {
                lifecycleScope.launch {
                    //val intent = Intent(Intent.ACTION_VIEW)
                    //intent.data = Uri.parse(it.url)
                    //startActivity(intent)

                    val action =
                        NewsFragmentDirections.actionNewsFragmentToNewsItemFragment(it.id)
                    findNavController().navigate(action)
                }
            }

            itemsAdapter.onClick = onItemClick

            itemsAdapter.swapItems(it.first, it.second)
        }
    }
}