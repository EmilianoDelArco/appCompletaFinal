package com.example.jetpackroom


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import com.example.jetpackroom.ui.theme.JetpackRoomTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jetpackroom.meteorologia.constant.Const.Companion.colorBg1
import com.example.jetpackroom.meteorologia.constant.Const.Companion.colorBg2
import com.example.jetpackroom.meteorologia.constant.Const.Companion.permissions
import com.example.jetpackroom.todo.db.Todo
import com.example.jetpackroom.meteorologia.model.MyLatLng
import com.example.jetpackroom.meteorologia.view.ForecastSection
import com.example.jetpackroom.meteorologia.view.WeatherSection
import com.example.jetpackroom.meteorologia.viewModel.MainViewModel
import com.example.jetpackroom.meteorologia.viewModel.STATE
import com.example.jetpackroom.todo.MainApplication
import com.example.jetpackroom.todo.TodoItem
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.*
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    private val dao = MainApplication.database.todoDao()
    private var todoList = mutableStateListOf<Todo>()
    private var scope = MainScope()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequired: Boolean = false
    private lateinit var mainViewModel: MainViewModel


    override fun onResume() {
        super.onResume()
        if (locationRequired) startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        locationCallback?.let {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 100
            )
                .setWaitForAccurateLocation(false)
                .setIntervalMillis(3000)
                .setMaxUpdateDelayMillis(100)
                .build()

            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initLocationClient()
        initViewModel()
        super.onCreate(savedInstanceState)
        setContent {
            var currentLocation by remember {
                mutableStateOf(MyLatLng(0.0, 0.0))
            }
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for (location in p0.locations) {
                        currentLocation = MyLatLng(
                            location.latitude,
                            location.longitude
                        )
                    }

                }
            }

            JetpackRoomTheme {

                var selectedItem by remember { mutableStateOf("home") }
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                ModalNavigationDrawer(

                    drawerState = drawerState,

                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(300.dp),
                            content = {
                                Text("Menu", modifier = Modifier.padding(10.dp))
                                Divider()
                                NavigationDrawerItem(
                                    label = { Text(text = "Home") },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = "") },
                                    selected = selectedItem == "home",
                                    onClick = {
                                        navController.navigate("home")
                                        selectedItem = "home"
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    },

                                    )
                                NavigationDrawerItem(
                                    label = { Text(text = "Lista de tareas") },
                                    icon = { Icon(Icons.Filled.List, contentDescription = "") },
                                    selected = selectedItem == "taskList",
                                    onClick = {
                                        navController.navigate("taskList")
                                        selectedItem = "taskList"
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text(text = "Tiempo en tu ciudad") },
                                    icon = { Icon(Icons.Filled.Star, contentDescription = "") },
                                    selected = selectedItem == "weather",
                                    onClick = {
                                        navController.navigate("weather")
                                        selectedItem = "weather"
                                        scope.launch {
                                            drawerState.apply {
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                ) {

                    Surface() {

                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(navController = navController)
                            }
                            composable("taskList") {
                                MainScreen(todoList = todoList)
                            }
                            composable("weather") {
                                LocationScreen(this@MainActivity, currentLocation)
                            }
                        }
                    }
                }
            }
            loadToDo()
        }
    }

    private fun fetchWeatherInformation(mainViewModel: MainViewModel, currentLocation: MyLatLng) {
        mainViewModel.state = STATE.LOADING
        mainViewModel.getWeatherByLocation(currentLocation)
        mainViewModel.getForecastByLocation(currentLocation)
        mainViewModel.state = STATE.SUCCESS
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
    }

    @Composable
    fun LoadingSection() {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    @Composable
    fun ErrorSection(errorMessage: String) {
        return Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = errorMessage, color = Color.White)
        }
    }

    private fun initLocationClient() {
        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(this)
    }


    private fun loadToDo() {
        scope.launch {
            withContext(Dispatchers.Default) {
                dao.getAll().forEach { todo ->
                    todoList.add(todo)
                }
            }
        }
    }

    private fun postTodo(title: String) {
        scope.launch {
            withContext(Dispatchers.Default) {
                dao.post(Todo(title = title))

                todoList.clear()
                loadToDo()
            }
        }
    }

    private fun updateTodo(todo: Todo) {
        scope.launch {
            withContext(Dispatchers.Default) {
                dao.update(todo)

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Tarea Actualizada", Toast.LENGTH_SHORT).show()
                }

                todoList.clear()
                loadToDo()
            }
        }
    }

    private fun deleteTodo(todo: Todo) {
        scope.launch {
            withContext(Dispatchers.Default) {
                dao.delete(todo)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Tarea Eliminada", Toast.LENGTH_SHORT).show()
                }
                todoList.clear()
                loadToDo()
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(todoList: SnapshotStateList<Todo>) {
        //    val context = LocalContext.current
        val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
        var text: String by remember {
            mutableStateOf("")
        }
        var isEditDialogVisible by remember { mutableStateOf(false) }
        var editingTodo by remember { mutableStateOf<Todo?>(null) }


        Column(
            modifier = Modifier.clickable {
                keyboardController?.hide()
            }
        ) {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.main_title)) },
                modifier = Modifier.background(Color.Magenta)

            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                items(todoList) { todo ->
                    key(todo.id) {
                        TodoItem(
                            todo = todo,
                            onClick = {
                                deleteTodo(todo)
                            },
                            onEditClick = {
                                editingTodo = todo
                                isEditDialogVisible = true

                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .border(
                            BorderStroke(2.dp, Color.Blue)
                        ),
                    //                    .background(Color.White),


                    label = { Text(text = stringResource(id = R.string.main_new_todo)) }
                )

                Spacer(modifier = Modifier.size(18.dp))

                IconButton(
                    onClick = {
                        if (text.isEmpty()) return@IconButton

                        postTodo(text)
                        text = ""
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(56.dp)
                        .background(Color.Magenta)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.main_add_todo),
                        tint = Color.White
                    )
                }

            }

            if (isEditDialogVisible) {
                EditTodoDialog(
                    todo = editingTodo!!,
                    onEditTodo = { editedTodo ->
                        // Lógica para guardar la tarea editada
                        // Puedes actualizar la lista de tareas o realizar otras acciones necesarias
                        // En este ejemplo, simplemente imprimo la tarea editada
                        println("Tarea editada: ${editedTodo.title}")

                        // Actualizar la tarea en la base de datos
                        updateTodo(editedTodo)

                        // Actualizar la lista observable
                        val updatedList = todoList.toMutableList()
                        val index = updatedList.indexOfFirst { it.id == editedTodo.id }
                        if (index != -1) {
                            updatedList[index] = editedTodo
                            todoList.clear()
                            todoList.addAll(updatedList)
                        }


                        // Cerrar el diálogo de edición
                        isEditDialogVisible = false
                        editingTodo = null
                    },
                    onDismiss = {
                        // Cerrar el diálogo de edición
                        isEditDialogVisible = false
                        editingTodo = null
                    }
                )
            }
        }
    }


    @Composable
    fun EditTodoDialog(
        todo: Todo,
        onEditTodo: (Todo) -> Unit,
        onDismiss: () -> Unit
    ) {
        var editedTitle by remember { mutableStateOf(todo.title) }

        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Nuevo título") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = {
                            onEditTodo(todo.copy(title = editedTitle))
                            onDismiss()
                        }) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavHostController) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deslizar para abrir el menú",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Agregar la imagen de bienvenida centrada
            Image(
                painter = painterResource(id = R.drawable.ap),
                contentDescription = null,
                modifier = Modifier.size(200.dp) // Tamaño de la imagen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir a la lista de tareas
            Button(
                onClick = {
                    navController.navigate("taskList")
                }
            ) {
                Text(text = "Ver Lista de Tareas")
            }

        }

    }

    @Composable
    private fun LocationScreen(context: Context, currentLocation: MyLatLng) {

        val lanchuerMultiplePermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val areGranted = permissionsMap.values.reduce { accepted, next ->
                accepted && next
            }
            if (areGranted) {
                locationRequired = true;
                startLocationUpdate();
                Toast.makeText(context, "Permiso aceptado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
        val systemUiController = rememberSystemUiController()
        DisposableEffect(key1 = true, effect = {
            systemUiController.isSystemBarsVisible = false
            onDispose {
                systemUiController.isSystemBarsVisible = true
            }
        })

        LaunchedEffect(key1 = currentLocation, block = {
            coroutineScope {
                if (permissions.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }) {
                    startLocationUpdate()
                } else {
                    lanchuerMultiplePermission.launch(permissions)

                }
            }
        })

        LaunchedEffect(key1 = true, block = {
            fetchWeatherInformation(mainViewModel, currentLocation)
        })
        val gradient = Brush.linearGradient(
            colors = listOf(Color(colorBg1), Color(colorBg2)),
            start = Offset(1000f, -1000f),
            end = Offset(1000f, -1000f)
        )

        Box(
            modifier = Modifier.fillMaxSize()
                .background(gradient),
            contentAlignment = Alignment.BottomCenter

        ) {
            val screenHeigth = LocalConfiguration.current.screenHeightDp.dp
            val marginTop = screenHeigth * 0.1f
            val marginTopPx = with(LocalDensity.current) { marginTop.toPx() }


            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(
                            placeable.width,
                            placeable.height + marginTopPx.toInt()
                        ) {
                            placeable.placeRelative(0, marginTopPx.toInt())
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (mainViewModel.state) {
                    STATE.LOADING -> {
                        LoadingSection()
                    }

                    STATE.FAILED -> {
                        ErrorSection(mainViewModel.errorMessage)
                    }

                    else -> {
                        WeatherSection(mainViewModel.weatherResponse)
                        ForecastSection(mainViewModel.forecastResponse)
                    }
                }

            }
            FloatingActionButton(
                onClick = {
                    fetchWeatherInformation(mainViewModel, currentLocation)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            {
                Icon(
                    Icons.Default.Refresh, contentDescription = "Add"
                )
            }
        }
    }

}


