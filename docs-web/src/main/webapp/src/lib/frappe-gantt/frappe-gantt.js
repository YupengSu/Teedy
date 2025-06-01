(function() {
  'use strict';

  var Gantt = function(wrapper, tasks, options) {
    this.wrapper = typeof wrapper === 'string' ? document.querySelector(wrapper) : wrapper;
    this.tasks = tasks;
    this.options = options || {};
    this.options.header_height = this.options.header_height || 50;
    this.options.column_width = this.options.column_width || 30;
    this.options.step = this.options.step || 24;
    this.options.view_modes = this.options.view_modes || ['Quarter Day', 'Half Day', 'Day', 'Week', 'Month'];
    this.options.view_mode = this.options.view_mode || 'Day';
    this.options.date_format = this.options.date_format || 'YYYY-MM-DD';
    this.options.language = this.options.language || 'en';
    this.options.custom_popup_html = this.options.custom_popup_html || null;

    this.init();
  };

  Gantt.prototype.init = function() {
    this.setup_wrapper();
    this.setup_tasks();
    this.setup_dates();
    this.setup_dependencies();
    this.render();
  };

  Gantt.prototype.setup_wrapper = function() {
    this.wrapper.classList.add('gantt');
  };

  Gantt.prototype.setup_tasks = function() {
    this.tasks = this.tasks.map(function(task, i) {
      task._index = i;
      task.start = moment(task.start);
      task.end = moment(task.end);
      return task;
    });
  };

  Gantt.prototype.setup_dates = function() {
    this.dates = [];
    var date = moment(this.tasks[0].start);
    while (date.isBefore(this.tasks[this.tasks.length - 1].end)) {
      this.dates.push(date.clone());
      date.add(1, 'days');
    }
  };

  Gantt.prototype.setup_dependencies = function() {
    this.dependencies = [];
    this.tasks.forEach(function(task) {
      if (task.dependencies) {
        var deps = Array.isArray(task.dependencies)
          ? task.dependencies
          : (typeof task.dependencies === 'string' && task.dependencies.length > 0
              ? task.dependencies.split(',') : []);
        deps.forEach(function(dependency) {
          this.dependencies.push({
            from: dependency,
            to: task.id
          });
        }, this);
      }
    }, this);
  };

  Gantt.prototype.render = function() {
    this.clear();
    this.setup_layers();
    this.make_grid();
    this.make_dates();
    this.make_bars();
    this.make_arrows();
    this.setup_events();
  };

  Gantt.prototype.clear = function() {
    this.wrapper.innerHTML = '';
  };

  Gantt.prototype.setup_layers = function() {
    this.layers = {};
    this.layers.grid = document.createElement('div');
    this.layers.grid.classList.add('grid');
    this.layers.date = document.createElement('div');
    this.layers.date.classList.add('date');
    this.layers.bar = document.createElement('div');
    this.layers.bar.classList.add('bar');
    this.layers.arrow = document.createElement('div');
    this.layers.arrow.classList.add('arrow');

    this.wrapper.appendChild(this.layers.grid);
    this.wrapper.appendChild(this.layers.date);
    this.wrapper.appendChild(this.layers.bar);
    this.wrapper.appendChild(this.layers.arrow);
  };

  Gantt.prototype.make_grid = function() {
    var grid_html = '';
    for (var i = 0; i < this.tasks.length; i++) {
      grid_html += '<div class="grid-row"></div>';
    }
    this.layers.grid.innerHTML = grid_html;
  };

  Gantt.prototype.make_dates = function() {
    var date_html = '';
    for (var i = 0; i < this.dates.length; i++) {
      date_html += '<div class="date-cell">' + this.dates[i].format('MM/DD') + '</div>';
    }
    this.layers.date.innerHTML = date_html;
  };

  Gantt.prototype.make_bars = function() {
    var bar_html = '';
    for (var i = 0; i < this.tasks.length; i++) {
      var task = this.tasks[i];
      var start = task.start.diff(this.dates[0], 'days');
      var end = task.end.diff(this.dates[0], 'days');
      var left = start * this.options.column_width;
      var width = (end - start) * this.options.column_width;
      bar_html += '<div class="bar-wrapper" data-id="' + task.id + '">' +
        '<div class="bar" style="left: ' + left + 'px; width: ' + width + 'px;"></div>' +
        '<div class="bar-label">' + task.name + '</div>' +
        '</div>';
    }
    this.layers.bar.innerHTML = bar_html;
  };

  Gantt.prototype.make_arrows = function() {
    var arrow_html = '';
    for (var i = 0; i < this.dependencies.length; i++) {
      var dependency = this.dependencies[i];
      var from_task = this.tasks.find(function(task) { return task.id === dependency.from; });
      var to_task = this.tasks.find(function(task) { return task.id === dependency.to; });
      if (from_task && to_task) {
        var from_start = from_task.start.diff(this.dates[0], 'days');
        var to_start = to_task.start.diff(this.dates[0], 'days');
        var from_left = from_start * this.options.column_width;
        var to_left = to_start * this.options.column_width;
        arrow_html += '<div class="arrow-wrapper">' +
          '<div class="arrow" style="left: ' + from_left + 'px; width: ' + (to_left - from_left) + 'px;"></div>' +
          '</div>';
      }
    }
    this.layers.arrow.innerHTML = arrow_html;
  };

  Gantt.prototype.setup_events = function() {
    var self = this;
    this.wrapper.addEventListener('click', function(e) {
      var bar_wrapper = e.target.closest('.bar-wrapper');
      if (bar_wrapper) {
        var task_id = bar_wrapper.getAttribute('data-id');
        var task = self.tasks.find(function(task) { return task.id === task_id; });
        if (task) {
          self.options.on_click(task);
        }
      }
    });
  };

  Gantt.prototype.change_view_mode = function(mode) {
    this.options.view_mode = mode;
    this.render();
  };

  window.Gantt = Gantt;
})(); 