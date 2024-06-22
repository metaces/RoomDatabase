package com.devspace.taskbeats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class CreateUpdateTaskBottomSheet(
    private val categoryList: List<CategoryEntity>,
    private val task: TaskUiData? = null,
    private val onCreateClicked: (TaskUiData)-> Unit,
    private val onUpdateClicked: (TaskUiData)-> Unit,
    private val onDelete: (TaskUiData)-> Unit,
): BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.create_update_task_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val btnCreateUpdate = view.findViewById<Button>(R.id.btn_task_create_update)
        val btnDelete = view.findViewById<Button>(R.id.btn_task_delete)
        val tieTaskName = view.findViewById<TextInputEditText>(R.id.tie_task_name)
        val spinner: Spinner = view.findViewById(R.id.spinner_category_list)

        val categoryListTemp = mutableListOf<String>("Select")
        categoryListTemp.addAll(
            categoryList.map { it.name }
        )
        val categoryStr: List<String> = categoryListTemp
        var taskCategory: String? = null

        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter(
            requireActivity().baseContext,
            android.R.layout.simple_spinner_item,
            categoryStr.toList()
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long) {
                taskCategory = categoryStr[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        if (task == null) {
            btnDelete.isVisible = false
            tvTitle.setText(R.string.create_task_title)
            btnCreateUpdate.setText(R.string.create)
        } else {
            btnDelete.isVisible = true
            tvTitle.setText(R.string.update_task_title)
            btnCreateUpdate.setText(R.string.update)

            tieTaskName.setText(task.name)

            val currentCategory = categoryList.first { it.name == task.category }
            val position = categoryList.indexOf(currentCategory)
            spinner.setSelection(position)
        }

        btnDelete.setOnClickListener {
            if(task != null){
                onDelete(task)
                dismiss()
            } else {
                Log.d("CreateUpdateTaskBottomSheet", "Task not found")
            }
        }

        btnCreateUpdate.setOnClickListener {
            val name = tieTaskName.text.toString()
            if (taskCategory != "Select" && name.trim().isNotEmpty()){

                if (task == null) {
                    onCreateClicked.invoke(
                        TaskUiData(
                            id = 0,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                } else {
                    onUpdateClicked.invoke(
                        TaskUiData(
                            id = task.id,
                            name = name,
                            category = requireNotNull(taskCategory)
                        )
                    )
                }
                dismiss()
            }else{
                Snackbar.make(btnCreateUpdate, "Please select a category", Snackbar.LENGTH_LONG).show()
            }
        }

        return view
    }
}