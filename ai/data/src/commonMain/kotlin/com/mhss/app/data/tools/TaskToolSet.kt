package com.mhss.app.data.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import com.mhss.app.data.llmDateTimeFormatUnicode
import com.mhss.app.data.nowMillis
import com.mhss.app.data.parseDateTimeFromLLM
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.domain.use_case.UpsertTasksUseCase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class TaskToolSet(
    private val upsertTask: UpsertTaskUseCase,
    private val upsertTasks: UpsertTasksUseCase,
    private val searchTasksByName: SearchTasksUseCase,
    private val getTask: GetTaskByIdUseCase,
    private val updateTaskCompletedUseCase: UpdateTaskCompletedUseCase
) {
    private val searchTasksTool = object : Tool<SearchTasksArgs, SearchTasksResult>(
        argsType = typeToken<SearchTasksArgs>(),
        resultType = typeToken<SearchTasksResult>(),
        name = SEARCH_TASKS_TOOL,
        description = "Search tasks by title (partial match). If the query is empty, returns all tasks."
    ) {
        override suspend fun execute(args: SearchTasksArgs): SearchTasksResult {
            val matchedTasks = searchTasksByName(args.query).first()
            return SearchTasksResult(
                tasks = matchedTasks.map { it.toToolResult() },
                sourceTasks = matchedTasks
            )
        }
    }

    private val createTaskTool = object : Tool<CreateTaskArgs, TaskIdResult>(
        argsType = typeToken<CreateTaskArgs>(),
        resultType = typeToken<TaskIdResult>(),
        name = CREATE_TASK_TOOL,
        description = "Create a task. `isCompleted` = false initially. Returns ID."
    ) {
        override suspend fun execute(args: CreateTaskArgs): TaskIdResult {
            val id = Uuid.generateV7().toString()
            val task = Task(
                title = args.title,
                description = args.description,
                priority = args.priority,
                dueDate = if (args.dueDate != null) {
                    args.dueDate.parseDateTimeFromLLM()
                        ?: throw IllegalArgumentException("Invalid due date format for date: ${args.dueDate}. The task was not created.")
                } else null,
                subTasks = args.subTasks?.map { SubTask(it.title, it.isCompleted) } ?: emptyList(),
                recurring = args.recurring,
                frequency = args.frequency,
                frequencyAmount = args.frequencyAmount,
                createdDate = nowMillis(),
                updatedDate = nowMillis(),
                id = id
            )
            upsertTask(task)
            return TaskIdResult(createdTaskId = id)
        }
    }

    private val updateTaskCompletedTool = object : Tool<UpdateTaskCompletedArgs, TaskResult>(
        argsType = typeToken<UpdateTaskCompletedArgs>(),
        resultType = typeToken<TaskResult>(),
        name = UPDATE_TASK_COMPLETED_TOOL,
        description = "Update task completed status."
    ) {
        override suspend fun execute(args: UpdateTaskCompletedArgs): TaskResult {
            val task = getTask(args.id)
                ?: throw IllegalArgumentException("Task with id ${args.id} not found. The operation did not proceed.")
            updateTaskCompletedUseCase(task, args.completed)
            return TaskResult(getTask(args.id)?.toToolResult())
        }
    }

    private val createMultipleTasksTool = object : Tool<CreateMultipleTasksArgs, TaskIdsResult>(
        argsType = typeToken<CreateMultipleTasksArgs>(),
        resultType = typeToken<TaskIdsResult>(),
        name = CREATE_MULTIPLE_TASKS_TOOL,
        description = "Create multiple tasks. Returns IDs."
    ) {
        override suspend fun execute(args: CreateMultipleTasksArgs): TaskIdsResult {
            val taskModels = args.tasks.map { input ->
                val id = Uuid.generateV7().toString()
                Task(
                    title = input.title,
                    description = input.description,
                    priority = input.priority,
                    dueDate = input.dueDate?.let {
                        it.parseDateTimeFromLLM()
                            ?: throw IllegalArgumentException("Invalid date format for task: ${input.title}. The tasks were not created.")
                    },
                    subTasks = input.subTasks?.map { SubTask(it.title, it.isCompleted) } ?: emptyList(),
                    recurring = input.recurring,
                    frequency = input.frequency,
                    frequencyAmount = input.frequencyAmount,
                    createdDate = nowMillis(),
                    updatedDate = nowMillis(),
                    id = id
                )
            }
            upsertTasks(taskModels)
            return TaskIdsResult(createdTaskIds = taskModels.map { it.id })
        }
    }

    val tools: List<ToolBase<*, *>> = listOf(
        searchTasksTool,
        createTaskTool,
        updateTaskCompletedTool,
        createMultipleTasksTool
    )
}

@Serializable
data class SearchTasksArgs(val query: String)

@Serializable
data class CreateTaskArgs(
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.LOW,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val dueDate: String? = null,
    val subTasks: List<SubTaskInput>? = null,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val frequencyAmount: Int = 1
)

@Serializable
data class UpdateTaskCompletedArgs(
    val id: String,
    val completed: Boolean
)

@Serializable
data class CreateMultipleTasksArgs(val tasks: List<TaskInput>)

@Serializable
data class TaskInput(
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.LOW,
    @property:LLMDescription("Format: $llmDateTimeFormatUnicode") val dueDate: String? = null,
    val subTasks: List<SubTaskInput>? = null,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val frequencyAmount: Int = 1
)

@Serializable
data class SubTaskInput(
    val title: String,
    val isCompleted: Boolean = false
)

@Serializable
data class SearchTasksResult(
    val tasks: List<TaskToolResult>,
    @Transient val sourceTasks: List<Task> = emptyList()
)

@Serializable
data class TaskIdResult(val createdTaskId: String)

@Serializable
data class TaskResult(val task: TaskToolResult?)

@Serializable
data class TaskIdsResult(val createdTaskIds: List<String>)
