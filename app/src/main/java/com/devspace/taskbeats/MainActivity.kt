package com.devspace.taskbeats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var tasks = listOf<TaskUiData>()

    private val categoryAdapter = CategoryListAdapter()
    private val taskAdapter by lazy {
        TaskListAdapter()
    }

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDataBase::class.java, "database-task-beat"
        ).build()
    }

    private val categoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        inserDefaultCategory()
//        insertDefaultTask()

        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        val fabCreateTask = findViewById<FloatingActionButton>(R.id.fab_create_task)

        fabCreateTask.setOnClickListener {
            showCreateUpdateTaskBottomSheet()
        }

        taskAdapter.setOnClickListener {
            showCreateUpdateTaskBottomSheet()
        }

        categoryAdapter.setOnClickListener { selected ->
            if(selected.name == "+") {
                val createCategoryBottomSheet = CreateCategoryBottomSheet { categoryName ->
                    val categoryEntity = CategoryEntity(
                        name = categoryName,
                        isSelected = false
                    )
                    inserCategory(categoryEntity)
                }

                createCategoryBottomSheet.show(supportFragmentManager, "createCategoryBottomSheet")
            }else{

                val categoryTemp = categories.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                val taskTemp =
                    if (selected.name != "ALL" ) {
                        tasks.filter { it.category == selected.name }
                    } else {
                        tasks
                    }
                taskAdapter.submitList(taskTemp)
                categoryAdapter.submitList(categoryTemp)
            }


        }

        rvCategory.adapter = categoryAdapter
        GlobalScope.launch(Dispatchers.IO) {
            getCategoriesDataBase()
        }

        rvTask.adapter = taskAdapter

        GlobalScope.launch(Dispatchers.IO) {
            getTasksDataBase()
        }
    }

    private fun showCreateUpdateTaskBottomSheet() {
        val createTaskBottomSheet = CreateTaskBottomSheet(
            categories
        ) { taskToBeCreated ->
            val taskEntityToBeInsert = TaskEntity(
                name = taskToBeCreated.name,
                category = taskToBeCreated.category
            )
            insertTask(taskEntityToBeInsert)
        }

        createTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")
    }

    private fun inserDefaultCategory() {
        val categoriesEntity = categories.map {
            CategoryEntity(
                name = it.name,
                isSelected = it.isSelected
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            categoriesEntity.forEach {categoryEntity ->
                categoryDao.insertAll(categoryEntity)
            }
        }
    }

    private fun getCategoriesDataBase() {

        val categoriesDb = categoryDao.getAll()
        val categoriesUiData = categoriesDb.map {
            CategoryUiData(
                name = it.name,
                isSelected = it.isSelected
            )
        }.toMutableList()

        categoriesUiData.add(
            CategoryUiData(
                name = "+",
                isSelected = false
            )
        )
        GlobalScope.launch(Dispatchers.Main) {
            categories = categoriesUiData
            categoryAdapter.submitList(categoriesUiData)
        }

    }

    private fun insertDefaultTask() {
        val tasksEntities = tasks.map {
            TaskEntity(
                name = it.name,
                category = it.category
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            tasksEntities.forEach { taskEntity ->
                taskDao.insertAll(taskEntity)
            }
        }
    }

    private fun getTasksDataBase() {
            val taskDb = taskDao.getAll()
            val taskUiData = taskDb.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
                tasks = taskUiData
                taskAdapter.submitList(taskUiData)
            }
    }

    private fun inserCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insert(categoryEntity)
            getCategoriesDataBase()
        }
    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTasksDataBase()
        }
    }
}
