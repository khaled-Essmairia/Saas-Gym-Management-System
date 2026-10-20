package com.gym.web.form;

import jakarta.validation.constraints.*;

/** Customer to-do form (to-do.php, update-todo.php). */
public class TodoForm {

    @NotBlank(message = "Please enter your task")
    @Size(max = 30, message = "Task: max 30 characters")
    private String taskDesc;

    @NotBlank
    private String taskStatus = "In Progress";

    public String getTaskDesc() { return taskDesc; }
    public void setTaskDesc(String taskDesc) { this.taskDesc = taskDesc; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
}
