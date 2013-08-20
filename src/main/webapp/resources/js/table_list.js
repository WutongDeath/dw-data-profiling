var TableList = function(opts) {
	var self = this;
	self.databaseId = opts.databaseId;
	self.tableNameList = opts.tableNameList;

	self.initList();
	self.initSearch();
	self.initPagination();
};

TableList.prototype = {
    constructor: TableList,

    initList: function() {
        var self = this;
        self.perPage = 15;
        self.resetList();
    },

    refreshList: function() {
    	var self = this;

    	$('#tblTables tr:gt(1)').remove();
    	$('#tblTables tr:eq(1)').show();

    	var tables = [];
    	var start = (self.currentPage - 1) * self.perPage;
    	for (var i = 0; i < self.perPage; ++i) {
    		if (start >= self.currentList.length) {
    			break;
    		}
    		tables.push(self.currentList[start + i]);
    	}

    	if (tables.length == 0) {
    		$('#tblTables tr:eq(1)').hide();
    		$('<tr><td colspan="6" align="center">No Result</td></tr>').appendTo('#tblTables');
    		return;
    	}

    	var data = {
    		databaseId: self.databaseId,
    		tables: tables.join(',')
    	};

    	$.getJSON('/table/get_info/', data, function(tableInfo) {

    		$('#tblTables tr:eq(1)').hide();

    		$.each(tables, function(i, tableName) {

    			if (!(tableName in tableInfo)) {
    				return true;
    			}

    			var tr = '<tr>'
    				   + '<td>' + (start + i + 1) + '</td>'
    				   + '<td>' + this + '</td>'
    				   + '<td>' + tableInfo[this].columnCount + '</td>'
    				   + '<td>' + tableInfo[this].rowCount + '</td>'
    				   + '<td>' + tableInfo[this].dataLength + '</td>';

    			if ('status' in tableInfo[this]) {

    				if (tableInfo[this].status == 1) {
    					tr += '<td>Processing</td>';
    				} else if (tableInfo[this].status == 2) {
    					tr += '<td>Processed</td>';
    				} else {
    					tr += '<td>Unknown</td>';
    				}
    				tr += '<td>' + tableInfo[this].updated + '</td>';

    			} else {
    				tr += '<td>-</td><td>-</td>';
    				tableInfo[this].status = 0;
    			}

    			var data = $.param({
    				databaseId: self.databaseId,
    				table: tableName
    			});

    			tr += '<td><a target="_blank" href="/column/list/?' + data + '">Detail</a>'
    			    + ' <a href="javascript:void(0)" profile="profile">Profile</a></td>'
    				+ '</tr>';

    			var $tr = $(tr);
    			$tr.find('a[profile]').click(function() {

    				if (tableInfo[tableName].status == 1) {
    					alert('Profiling is in progress!');
    					return false;
    				}

    				$.post('/table/start_profiling/', data, function(result) {
    					if (result.status == 'ok') {
    						self.refreshList();
    					} else {
    						alert(result.msg);
    					}
    				}, 'json');

    			});

    			$tr.appendTo('#tblTables');
    		});

    	});
    },

    resetList: function() {
    	var self = this;
    	self.currentList = self.tableNameList;
    	self.currentPage = 1;
    	self.totalPage = Math.ceil(self.currentList.length / self.perPage);
    	self.refreshList();
    },

    initSearch: function() {
    	var self = this;

    	var timeoutHandler = null;
    	$('#txtSearch').keyup(function() {
    		if (timeoutHandler != null) {
    			clearTimeout(timeoutHandler);
    		}
    		timeoutHandler = setTimeout(function() {
    			self.search();
    		}, 500);
    	});
    },

    search: function() {
    	var self = this;

    	var keyword = $.trim($('#txtSearch').val());
    	if (!keyword) {
    		self.resetList();
    		return;
    	}

    	keyword = keyword.toLowerCase();
    	self.currentList = [];
    	$.each(self.tableNameList, function() {
    		if (this.toLowerCase().indexOf(keyword) != -1) {
    			self.currentList.push(this);
    		}
    	});

    	self.currentPage = 1;
    	self.totalPage = Math.ceil(self.currentList.length / self.perPage);
    	self.refreshList();
    },

    initPagination: function() {
    	var self = this;

    	$('#btnPage').click(function() {
    		var page = parseInt($('#txtPage').val());
    		if (!page) {
    			return false;
    		}
    		if (page < 1 || page > self.totalPage) {
    			return false;
    		}
    		self.currentPage = page;
    		self.refreshList();
    	});

    },

    _theEnd: undefined
};
