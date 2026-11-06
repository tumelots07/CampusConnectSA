
package com.example.campusconnectsa

import android.app.DatePickerDialog
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusconnectsa.data.AcademicTaskData
import com.example.campusconnectsa.data.AcademicTaskRequest
import com.example.campusconnectsa.data.AcademicTaskUpdateRequest
import com.example.campusconnectsa.data.ApiClient
import com.example.campusconnectsa.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AcademicPlannerActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var moduleInput: EditText
    private lateinit var typeSpinner: Spinner
    private lateinit var prioritySpinner: Spinner
    private lateinit var dateButton: Button
    private lateinit var saveButton: Button

    private lateinit var taskContainer: LinearLayout
    private lateinit var emptyMessage: TextView
    private lateinit var weekStrip: LinearLayout

    private lateinit var totalTasks: TextView
    private lateinit var completedTasks: TextView
    private lateinit var pendingTasks: TextView
    private lateinit var progressText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var sessionManager: SessionManager

    private val tasks = mutableListOf<AcademicTaskData>()

    private var selectedDate = ""
    private var editingTaskId: String? = null
    private var filterDate = ""
    private var loading = false

    private val storageFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val displayFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val dayFormat =
        SimpleDateFormat("EEE", Locale.getDefault())

    private val numberFormat =
        SimpleDateFormat("dd", Locale.getDefault())

    private val taskTypes = listOf(
        "Assignment",
        "Test",
        "Examination",
        "Project",
        "Other"
    )

    private val priorities = listOf(
        "Low",
        "Medium",
        "High"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_academic_planner)

        sessionManager = SessionManager(this)

        initializeViews()
        setupSpinners()
        setupListeners()

        filterDate = storageFormat.format(Calendar.getInstance().time)

        buildWeekStrip()
        loadTasks()
    }

    override fun onResume() {
        super.onResume()

        if (::taskContainer.isInitialized && !loading) {
            loadTasks()
        }
    }

    private fun initializeViews() {

        titleInput = findViewById(R.id.etTaskTitle)
        moduleInput = findViewById(R.id.etTaskModule)

        typeSpinner = findViewById(R.id.spTaskType)
        prioritySpinner = findViewById(R.id.spTaskPriority)

        dateButton = findViewById(R.id.btnChooseDate)
        saveButton = findViewById(R.id.btnSaveTask)

        taskContainer = findViewById(R.id.taskListContainer)
        emptyMessage = findViewById(R.id.tvEmptyTasks)
        weekStrip = findViewById(R.id.weekStrip)

        totalTasks = findViewById(R.id.tvTotalTasks)
        completedTasks = findViewById(R.id.tvCompletedTasks)
        pendingTasks = findViewById(R.id.tvPendingTasks)

        progressText = findViewById(R.id.tvPlannerProgress)
        progressBar = findViewById(R.id.progressPlanner)
    }

    private fun setupSpinners() {

        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            taskTypes
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

        prioritySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorities
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }
    }

    private fun setupListeners() {

        dateButton.setOnClickListener {
            showDatePicker()
        }

        saveButton.setOnClickListener {
            saveTask()
        }
    }

    // ------------------------------------------------
    // LOAD ACTIVITIES FROM MONGODB
    // ------------------------------------------------

    private fun loadTasks() {

        val authToken = sessionManager.getToken()

        if (authToken.isNullOrBlank()) {
            showMessage("Your session has expired. Please log in again.")
            return
        }

        if (loading) return

        loading = true

        lifecycleScope.launch {

            try {

                val response = withContext(Dispatchers.IO) {
                    ApiClient.apiService.getTasks(
                        "Bearer $authToken"
                    )
                }

                if (response.isSuccessful) {

                    tasks.clear()

                    tasks.addAll(
                        response.body()?.tasks.orEmpty()
                    )

                    displayTasks()

                } else {

                    showMessage(
                        apiMessage(
                            response.errorBody()?.string(),
                            "Could not load academic activities."
                        )
                    )
                }

            } catch (e: Exception) {

                showMessage(
                    "Unable to connect to the server. " +
                            "Check your Wi-Fi and make sure the CampusConnect API is running."
                )

            } finally {

                loading = false
            }
        }
    }

    // ------------------------------------------------
    // WEEK SELECTOR
    // ------------------------------------------------

    private fun buildWeekStrip() {

        weekStrip.removeAllViews()

        val calendar = Calendar.getInstance()

        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0 until 7) {

            val date = storageFormat.format(calendar.time)

            val label =
                "${dayFormat.format(calendar.time)}\n" +
                        numberFormat.format(calendar.time)

            val selected = date == filterDate

            val dayButton = TextView(this).apply {

                text = label
                gravity = Gravity.CENTER
                textSize = 13f

                setPadding(13, 14, 13, 14)

                setTextColor(
                    if (selected) {
                        getColor(android.R.color.white)
                    } else {
                        getColor(R.color.cc_navy)
                    }
                )

                setBackgroundColor(
                    if (selected) {
                        getColor(R.color.cc_primary)
                    } else {
                        getColor(R.color.cc_card)
                    }
                )

                setOnClickListener {

                    filterDate = date

                    buildWeekStrip()
                    displayTasks()
                }
            }

            val params = LinearLayout.LayoutParams(
                66,
                70
            )

            params.setMargins(0, 0, 9, 0)

            weekStrip.addView(dayButton, params)

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // ------------------------------------------------
    // DATE PICKER
    // ------------------------------------------------

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                calendar.set(year, month, day)

                selectedDate =
                    storageFormat.format(calendar.time)

                filterDate = selectedDate

                dateButton.text =
                    "📅  ${displayFormat.format(calendar.time)}"

                buildWeekStrip()
                displayTasks()

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ------------------------------------------------
    // ADD OR UPDATE ACTIVITY
    // ------------------------------------------------

    private fun saveTask() {

        val authToken = sessionManager.getToken()

        if (authToken.isNullOrBlank()) {
            showMessage("Please log in again before saving an activity.")
            return
        }

        val title = titleInput.text.toString().trim()
        val module = moduleInput.text.toString().trim()

        if (title.length < 2) {
            titleInput.error = "Enter an activity title"
            titleInput.requestFocus()
            return
        }

        if (module.isBlank()) {
            moduleInput.error = "Enter a module name or code"
            moduleInput.requestFocus()
            return
        }

        if (selectedDate.isBlank()) {
            showMessage("Please select a due date.")
            return
        }

        val savedDate = selectedDate

        val request = AcademicTaskRequest(
            title = title,
            taskType = typeSpinner.selectedItem.toString(),
            module = module,
            priority = prioritySpinner.selectedItem.toString(),
            dueDate = "${savedDate}T12:00:00.000Z"
        )

        saveButton.isEnabled = false

        lifecycleScope.launch {

            try {

                val response = withContext(Dispatchers.IO) {

                    val editingId = editingTaskId

                    if (editingId == null) {

                        ApiClient.apiService.createTask(
                            "Bearer $authToken",
                            request
                        )

                    } else {

                        ApiClient.apiService.updateTask(
                            "Bearer $authToken",
                            editingId,
                            AcademicTaskUpdateRequest(
                                title = request.title,
                                taskType = request.taskType,
                                module = request.module,
                                priority = request.priority,
                                dueDate = request.dueDate
                            )
                        )
                    }
                }

                if (response.isSuccessful) {

                    showMessage(
                        if (editingTaskId == null) {
                            "Academic activity added successfully."
                        } else {
                            "Academic activity updated successfully."
                        }
                    )

                    clearForm()

                    filterDate = savedDate

                    buildWeekStrip()
                    loadTasks()

                } else {

                    showMessage(
                        apiMessage(
                            response.errorBody()?.string(),
                            "Could not save this activity."
                        )
                    )
                }

            } catch (e: Exception) {

                showMessage(
                    "Unable to save the activity. Check your internet connection."
                )

            } finally {

                saveButton.isEnabled = true
            }
        }
    }

    // ------------------------------------------------
    // DISPLAY ACTIVITIES
    // ------------------------------------------------

    private fun displayTasks() {

        taskContainer.removeAllViews()

        val filteredTasks = tasks.filter {
            dateOnly(it.dueDate) == filterDate
        }

        emptyMessage.visibility =
            if (filteredTasks.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        emptyMessage.text =
            "No activities for this day. Add one or select another date."

        filteredTasks.forEach { task ->

            val card = LinearLayout(this).apply {

                orientation = LinearLayout.VERTICAL

                setPadding(18, 16, 18, 16)

                setBackgroundResource(
                    R.drawable.bg_planner_card
                )
            }

            val heading = TextView(this).apply {

                text = task.title
                textSize = 17f

                setTextColor(
                    getColor(R.color.cc_navy)
                )

                setTypeface(
                    null,
                    Typeface.BOLD
                )

                if (task.completed) {
                    paintFlags =
                        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

            val details = TextView(this).apply {

                text =
                    "${task.taskType}  •  ${task.module.orEmpty()}"

                textSize = 13f

                setTextColor(
                    getColor(R.color.cc_secondary_text)
                )

                setPadding(0, 7, 0, 7)
            }

            val priority = TextView(this).apply {

                text = "Priority: ${task.priority}"
                textSize = 12f

                setTextColor(
                    when (task.priority) {
                        "High" -> getColor(R.color.cc_danger)
                        "Medium" -> getColor(R.color.cc_warning)
                        else -> getColor(R.color.cc_success)
                    }
                )
            }

            val dueDate = TextView(this).apply {

                text = "Due: ${formatDate(task.dueDate)}"
                textSize = 12f

                setTextColor(
                    getColor(R.color.cc_secondary_text)
                )

                setPadding(0, 7, 0, 7)
            }

            val completedCheck = CheckBox(this).apply {

                text = "Mark as completed"
                textSize = 13f

                setTextColor(
                    getColor(R.color.cc_body_text)
                )

                isChecked = task.completed

                // Set listener after initial checkbox state.
                setOnCheckedChangeListener { _, checked ->
                    updateCompletion(task, checked)
                }
            }

            val actionRow = LinearLayout(this).apply {

                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val editButton = Button(this).apply {

                text = "Edit"
                isAllCaps = false

                setOnClickListener {
                    editTask(task)
                }
            }

            val deleteButton = Button(this).apply {

                text = "Delete"
                isAllCaps = false

                setOnClickListener {
                    confirmDelete(task)
                }
            }

            actionRow.addView(editButton)
            actionRow.addView(deleteButton)

            card.addView(heading)
            card.addView(details)
            card.addView(priority)
            card.addView(dueDate)
            card.addView(completedCheck)
            card.addView(actionRow)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, 14)

            taskContainer.addView(card, params)
        }

        updateStatistics()
    }

    // ------------------------------------------------
    // ACADEMIC STATISTICS
    // ------------------------------------------------

    private fun updateStatistics() {

        val total = tasks.size

        val completed = tasks.count {
            it.completed
        }

        val pending = total - completed

        val percentage =
            if (total == 0) {
                0
            } else {
                (completed * 100) / total
            }

        totalTasks.text = total.toString()
        completedTasks.text = completed.toString()
        pendingTasks.text = pending.toString()

        progressText.text = "$percentage%"
        progressBar.progress = percentage
    }

    // ------------------------------------------------
    // MARK ACTIVITY AS COMPLETED
    // ------------------------------------------------

    private fun updateCompletion(
        task: AcademicTaskData,
        completed: Boolean
    ) {

        val authToken = sessionManager.getToken() ?: return

        val request = AcademicTaskUpdateRequest(
            completed = completed
        )

        lifecycleScope.launch {

            try {

                val response = withContext(Dispatchers.IO) {

                    ApiClient.apiService.updateTask(
                        "Bearer $authToken",
                        task.id,
                        request
                    )
                }

                if (response.isSuccessful) {

                    loadTasks()

                } else {

                    showMessage(
                        apiMessage(
                            response.errorBody()?.string(),
                            "Could not update completion status."
                        )
                    )

                    loadTasks()
                }

            } catch (e: Exception) {

                showMessage("Unable to update this activity.")

                loadTasks()
            }
        }
    }

    // ------------------------------------------------
    // EDIT ACTIVITY
    // ------------------------------------------------

    private fun editTask(task: AcademicTaskData) {

        titleInput.setText(task.title)
        moduleInput.setText(task.module.orEmpty())

        typeSpinner.setSelection(
            taskTypes.indexOf(task.taskType).coerceAtLeast(0)
        )

        prioritySpinner.setSelection(
            priorities.indexOf(task.priority).coerceAtLeast(0)
        )

        selectedDate = dateOnly(task.dueDate)
        filterDate = selectedDate

        dateButton.text =
            "📅  ${formatDate(task.dueDate)}"

        editingTaskId = task.id

        saveButton.text = "Update Activity"

        buildWeekStrip()
    }

    // ------------------------------------------------
    // DELETE ACTIVITY
    // ------------------------------------------------

    private fun confirmDelete(task: AcademicTaskData) {

        AlertDialog.Builder(this)
            .setTitle("Delete activity")
            .setMessage(
                "Are you sure you want to delete '${task.title}'?"
            )
            .setPositiveButton("Delete") { _, _ ->
                deleteTask(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(task: AcademicTaskData) {

        val authToken = sessionManager.getToken() ?: return

        lifecycleScope.launch {

            try {

                val response = withContext(Dispatchers.IO) {

                    ApiClient.apiService.deleteTask(
                        "Bearer $authToken",
                        task.id
                    )
                }

                if (response.isSuccessful) {

                    showMessage("Academic activity deleted.")

                    if (editingTaskId == task.id) {
                        clearForm()
                    }

                    loadTasks()

                } else {

                    showMessage(
                        apiMessage(
                            response.errorBody()?.string(),
                            "Could not delete this activity."
                        )
                    )
                }

            } catch (e: Exception) {

                showMessage("Unable to delete this activity.")
            }
        }
    }

    // ------------------------------------------------
    // CLEAR FORM
    // ------------------------------------------------

    private fun clearForm() {

        titleInput.text.clear()
        moduleInput.text.clear()

        typeSpinner.setSelection(0)
        prioritySpinner.setSelection(0)

        selectedDate = ""
        editingTaskId = null

        dateButton.text = "📅  Choose Due Date"
        saveButton.text = "＋  Add Task"
    }

    // ------------------------------------------------
    // DATE HELPERS
    // ------------------------------------------------

    private fun dateOnly(value: String): String {
        return value.take(10)
    }

    private fun formatDate(value: String): String {

        return try {

            displayFormat.format(
                storageFormat.parse(dateOnly(value))!!
            )

        } catch (_: Exception) {

            dateOnly(value)
        }
    }

    // ------------------------------------------------
    // API ERROR MESSAGE
    // ------------------------------------------------

    private fun apiMessage(
        body: String?,
        fallback: String
    ): String {

        return try {

            JSONObject(body ?: "{}")
                .optString("message", fallback)
                .ifBlank { fallback }

        } catch (_: Exception) {

            fallback
        }
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
