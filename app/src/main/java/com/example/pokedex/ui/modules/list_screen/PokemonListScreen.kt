package com.example.pokedex.ui.modules.list_screen

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.filter
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.pokedex.R
import com.example.pokedex.data.models.PokemonItemData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun PokemonListScreen(navController: NavController) {
    val viewModel: PokemonListViewModel = hiltViewModel()
    var searchQuery by remember {
        mutableStateOf("")
    }
    val pager = remember {
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                prefetchDistance = 200,
                initialLoadSize = 20
            )
        ) {
            viewModel.PokemonPagingSource()
        }
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_international_pok_mon_logo),
                contentDescription = "Pokemon",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )

            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), hint = "Search"
            ) {
                searchQuery = it
            }

            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (searchQuery.isNotEmpty()) {
                    val results = lazyPagingItems.itemSnapshotList.toList().filterNotNull().filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                    if (results.isEmpty()) {
                        item {
                            Text(text = "No Items to found", textAlign = TextAlign.Center)
                        }
                    } else
                        items(results) { item ->
                            val number = item.url.split("/").last { it.isNotEmpty() }.toInt()
                            PokemonListItem(
                                item = PokemonItemData(
                                    name = item.name,
                                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/$number.png",
                                    number = number
                                ), getDominantColor = viewModel::calcMainColor
                            ) { it, color ->
                                navController.navigate("detail_screen/${it.name}/${color.toArgb()}")
                            }
                        }

                } else items(
                    lazyPagingItems.itemCount
                ) { index ->
                    val item = lazyPagingItems[index]
                    item?.let {
                        val number = item.url.split("/").last { it.isNotEmpty() }.toInt()
                        PokemonListItem(
                            item = PokemonItemData(
                                name = item.name,
                                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/$number.png",
                                number = number
                            ), getDominantColor = viewModel::calcMainColor
                        ) { it, color ->
                            navController.navigate("detail_screen/${it.name}/${color.toArgb()}")
                        }
                    } ?: run {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonListItem(
    modifier: Modifier = Modifier,
    item: PokemonItemData,
    getDominantColor: suspend (Drawable) -> Color,
    onTap: (PokemonItemData, Color) -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    var dominantColor by remember {
        mutableStateOf(surfaceColor)
    }
    val scope = rememberCoroutineScope()
    var loadingImage by remember {
        mutableStateOf(true)
    }

    val context = LocalContext.current
    val model = remember {
        ImageRequest.Builder(context).data(item.imageUrl)
            .listener(onSuccess = { imageRequest, successResult ->
                if (dominantColor == surfaceColor) {
                    val exceptionHandler =
                        CoroutineExceptionHandler { coroutineContext, throwable ->
                            dominantColor = surfaceColor
                        }
                    scope.launch(exceptionHandler) {
                        dominantColor = getDominantColor(successResult.drawable)
                    }
                }
            }).build()
    }
    Box(
        modifier = modifier
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominantColor, surfaceColor
                    )
                )
            )
            .clickable {
                onTap(item, dominantColor)
            }, contentAlignment = Alignment.Center
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = model,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center),
                    onState = {
                        loadingImage = when (it) {
                            AsyncImagePainter.State.Empty -> true
                            is AsyncImagePainter.State.Error -> true
                            is AsyncImagePainter.State.Loading -> true
                            is AsyncImagePainter.State.Success -> false
                        }
                    })
                if (loadingImage) CircularProgressIndicator(
                    modifier = Modifier.scale(0.5f), color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = item.name,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier, hint: String, onSearch: (String) -> Unit
) {
    var value: String by remember {
        mutableStateOf("")
    }
    var isHintDisplayed by remember {
        mutableStateOf(hint.isNotEmpty())
    }
    Box(modifier = modifier) {
        BasicTextField(value = value,
            onValueChange = {
                value = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, shape = CircleShape)
                .background(Color.White, shape = CircleShape)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplayed = !it.isFocused
                }) {
            if (isHintDisplayed) Text(hint, color = Color.LightGray)
            else it()
        }
    }
}
