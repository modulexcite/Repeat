function registerTableTasksOnClick() {
    var table = document.getElementById("tableTasks");
    if (table == null) {
        return;
    }

    // Start from row = 1 since row = 0 is the headings.
    for (var i = 1; i < table.rows.length; i++) {
        for (var j = 0; j < table.rows[i].cells.length; j++)
        table.rows[i].cells[j].onclick = function(cell, i, j) {
            return function() {
                // Minus one so that row index starts from 0.
                tableTaskOnClick(cell, i - 1, j);
            };
        }(table.rows[i].cells[j], i, j);
    }

    $('#modal-task-name-save').click(function() {
        newNameOnClick();
    });
}

function tableTaskOnClick(cell, row, col) {
    if (col == 0) { // Name.
        $('#modal-task-name-row').val(row);
        $('#new-task-name').val(cell.textContent);
        $('#modal-task-name').modal();
    }

    if (col == 1) { // Activation.
        window.location.assign("/task-activation?task=" + row);
    }

    if (col == 2) { // Enable/disable.
        $.post("/internals/toggle/task-enabled", JSON.stringify({task: row}), function(status){
        }).fail(function(response) {
            alert('Error toggling state: ' + response.responseText);
        });
    }
}

function newNameOnClick() {
    var row = $('#modal-task-name-row').val();
    var newName = $('#new-task-name').val();

    $.post("/internals/modify/task-name", JSON.stringify({task: row, name: newName}), function(status){
    }).fail(function(response) {
        alert('Error changing name: ' + response.responseText);
    });
}