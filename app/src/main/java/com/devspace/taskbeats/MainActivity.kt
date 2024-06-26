package com.devspace.taskbeats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var categories = listOf<CategoryUiData>()
    private var categoriesEntity = listOf<CategoryEntity>()
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

        taskAdapter.setOnClickListener {task ->
            showCreateUpdateTaskBottomSheet(task)
        }

        categoryAdapter.setOnLongClickListener {categoryToBeDeleted ->

            if( categoryToBeDeleted.name != "+" && categoryToBeDeleted.name != "ALL" ) {
                val title: String = this.getString(R.string.category_delete_title)
                val description: String = this.getString(R.string.category_delete_description)
                val btnText: String = this.getString(R.string.delete)

                showInfoDialog(title, description, btnText) {
                    val categoryEntityToBeDelete = CategoryEntity(
                       categoryToBeDeleted.name,
                       categoryToBeDeleted.isSelected
                   )
                   deleteCategory(categoryEntityToBeDelete)
                }
            }
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
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = true)

                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)

                        item.name != selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }

                if (selected.name != "ALL" ) {
                    filterCategory(selected.name)
//                    tasks.filter { it.category == selected.name }
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        getTasksDataBase()
                    }
                }
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

    private fun showCreateUpdateTaskBottomSheet(taskUiData: TaskUiData? = null) {
        val createUpdateTaskBottomSheet = CreateUpdateTaskBottomSheet(
            task = taskUiData,
            categoryList =  categoriesEntity,
            onCreateClicked = {taskToBeCreated ->
                val taskEntityToBeInsert = TaskEntity(
                    name = taskToBeCreated.name,
                    category = taskToBeCreated.category
                )
                insertTask(taskEntityToBeInsert)
            },
            onUpdateClicked = {taskToBeUpdated ->
                val taskEntityToBeUpdate = TaskEntity(
                    id = taskToBeUpdated.id,
                    name = taskToBeUpdated.name,
                    category = taskToBeUpdated.category
                )
                updateTask(taskEntityToBeUpdate)
            },
            onDelete = {taskToBeDeleted ->
                val taskEntityToBeDelete = TaskEntity(
                    id = taskToBeDeleted.id,
                    name = taskToBeDeleted.name,
                    category = taskToBeDeleted.category
                )
                deleteTask(taskEntityToBeDelete)
            }
        )

        createUpdateTaskBottomSheet.show(supportFragmentManager, "createTaskBottomSheet")
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
        categoriesEntity = categoriesDb
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
        val categoryListTemp =  mutableListOf(
            CategoryUiData(
                name = "All",
                isSelected = true
            )
        )

        categoryListTemp.addAll(categoriesUiData)
        GlobalScope.launch(Dispatchers.Main) {
            categories = categoryListTemp
            categoryAdapter.submitList(categories)
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

    private fun filterCategory(name: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksFromDataBase = taskDao.getAllByCategory(name)
            val taskUiData = tasksFromDataBase.map {
                TaskUiData(
                    id = it.id,
                    name = it.name,
                    category = it.category
                )
            }
            GlobalScope.launch(Dispatchers.Main) {
//                tasks = taskUiData
                taskAdapter.submitList(taskUiData)
            }
        }
    }

    private fun deleteCategory(categoryEntity: CategoryEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            val tasksToBeDeleted = taskDao.getAllByCategory(categoryEntity.name)
            taskDao.deleteAll(tasksToBeDeleted)
            categoryDao.delete(categoryEntity)
            getCategoriesDataBase()
            getTasksDataBase()
        }
    }

    private fun insertTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.insert(taskEntity)
            getTasksDataBase()
        }
    }

    private fun updateTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.update(taskEntity)
            getTasksDataBase()
        }
    }

    private fun deleteTask(taskEntity: TaskEntity) {
        GlobalScope.launch(Dispatchers.IO) {
            taskDao.delete(taskEntity)
            getTasksDataBase()
        }
    }

    private fun showInfoDialog(
        title: String,
        description: String,
        btnText: String,
        onClick: () -> Unit
    ) {
        val infoBottomSheet = InfoBottomSheet(
            title = title,
            description = description,
            btnText = btnText,
            onClicked = onClick
        )

        infoBottomSheet.show(supportFragmentManager, "InfoBottomSheet")

    }
}
