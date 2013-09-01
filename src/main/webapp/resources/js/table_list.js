var TableList = function(opts) {
    var self = this;
    self.databaseId = opts.databaseId;
    self.tableNameList = opts.tableNameList;
    self.contextPath = opts.contextPath;
    self.tplInfo = Handlebars.compile($('#tplInfo').html());
    self.tplDetails = Handlebars.compile($('#tplDetails').html());

    self.initList();
    self.initSearch();
};

TableList.prototype = {
    constructor: TableList,

    initList: function() {
        var self = this;
        self.perPage = 32;
        self.resetList();
    },

    refreshList: function() {
        var self = this;
        var divPagi = $('#divTables .pagination');

        $('#ulTables li').remove();

        var tables = [];
        var start = (self.currentPage - 1) * self.perPage;
        for (var i = 0; i < self.perPage; ++i) {
            if (start + i >= self.currentList.length) {
                break;
            }
            tables.push(self.currentList[start + i]);
        }

        if (tables.length == 0) {
            $('<li><strong>No result.</strong></li>').appendTo('#ulTables');
            divPagi.hide();
            return;
        }

        $.each(tables, function(i, tableName) {
            var li = '<li>'
                   + '<a href="' + self.contextPath + '/table/list/' + self.databaseId
                   + '?' + $.param({table: tableName}) + '" title="' + tableName + '">'
                   + '<i class="icon-list-alt"></i> ' + tableName
                   + '</a></li>';
            var $li = $(li);

            $li.find('a').click(function() {
                self.refreshInfo(tableName);
                return false;
            });

            $li.appendTo('#ulTables');
        });

        // pagination
        var pOffset = 0;
        var pStart = self.currentPage - 4;
        if (pStart < 1) {
            pOffset = 1 - pStart;
            pStart = 1;
        }
        var pEnd = self.currentPage + 5 + pOffset;
        if (pEnd > self.totalPage) {
            pEnd = self.totalPage;
        }

        divPagi.find('ul li').remove();
        divPagi.find('ul').append(
                '<li' + (self.currentPage == 1 ? ' class="disabled"' : '') + '>'
                + '<a href="javascript:void(0);" page="' + (self.currentPage - 1) + '">Prev</a>'
                + '</li>');
        for (var p = pStart; p <= pEnd; ++p) {
            divPagi.find('ul').append(
                    '<li' + (p == self.currentPage ? ' class="active"' : '') + '>'
                    + '<a href="javascript:void(0);" page="' + p + '">' + p + '</a>'
                    + '</li>');
        }
        divPagi.find('ul').append(
                '<li' + (self.currentPage == self.totalPage ? ' class="disabled"' : '') + '>'
                + '<a href="javascript:void(0);" page="' + (self.currentPage + 1) + '">Next</a>'
                + '</li>');

        divPagi.find('a').click(function() {
            var li = $(this).parent();
            if (li.is('.disabled') || li.is('.active')) {
                return false;
            }
            self.currentPage = parseInt($(this).attr('page'));
            self.refreshList();
        });

        divPagi.show();

    },

    resetList: function() {
        var self = this;
        self.currentList = self.tableNameList;
        self.currentPage = 1;
        self.totalPage = Math.ceil(self.currentList.length / self.perPage);
        self.refreshList();
    },

    currentTableName: null,
    refreshHandler: null,
    refreshInfo: function(tableName) {
        var self = this;

        self.currentTableName = tableName;

        var data = {
            databaseId: self.databaseId,
            table: tableName
        };

        $.getJSON(self.contextPath + '/table/get_info/', data, function(tableInfo) {

            if (self.currentTableName != tableInfo.tableName) {
                return;
            }

            if ($.isEmptyObject(tableInfo)) {
                alert("Fail to fetch table information.");
                $('#divTables').show();
                $('#divInfo').hide();
                return;
            }

            self.renderTable(tableInfo);
        });
    },

    renderTable: function(tableInfo) {
        var self = this;

        $('#divInfo').html(self.tplInfo(tableInfo));

        $('#divInfo').find('span[comment]').each(function() {
            if ($(this).attr('title')) {
                $(this).tooltip();
                $(this).css({
                    'border-bottom': '1px dashed black',
                    'cursor': 'pointer'
                });
            }
        });

        $('#divInfo').find('a[back]').click(function() {
            self.currentTableName = null;
            clearTimeout(self.refreshHandler);
            self.refreshHandler = null;
            $('#divTables').show();
            $('#divInfo').hide();
        });

        $('#divInfo').find('a[refresh]').click(function() {
            if (self.currentTableName != tableInfo.tableName) {
                return false;
            }
            clearTimeout(self.refreshHandler);
            self.refreshHandler = null;
            self.refreshInfo(tableInfo.tableName);
        });

        if (tableInfo.status == 1) { // processing

            $('#divInfo').find('#tdStatus').html(
                    '<div class="progress progress-striped active" style="width: 60px; float: left; margin-right: 10px;">'
                    + '<div class="bar" style="width: ' + tableInfo.progress + '%;"></div>'
                    + '</div>'
                    + tableInfo.progress + '%');

            $('#divInfo').find('#btnProfiling').addClass('disabled');

            if (self.refreshHandler == null) {
                self.refreshHandler = setTimeout(function() {
                    self.refreshHandler = null;
                    self.refreshInfo(tableInfo.tableName);
                }, 3000);
            }

        } else {

            var lblStatus;
            if (tableInfo.status == 2) { // processed
                lblStatus = '<span class="label label-success">Processed</span>';
            } else if (tableInfo.status == 3) { // error
                lblStatus = '<span class="label label-important">Error</span>';
            } else { // not profiled
                lblStatus = '<span class="label">Not Profiled</span>';
            }
            $('#divInfo').find('#tdStatus').html(lblStatus);

            if (tableInfo.rowCount == 0 || tableInfo.dataLength > 512 * 1024 * 1024) {
                $('#divInfo').find('#btnProfiling').addClass('disabled').attr('title', 'No data or too large.');
            } else {
                $('#divInfo').find('#btnProfiling').click(function() {

                    var data = {
                        databaseId: self.databaseId,
                        table: tableInfo.tableName
                    };

                    $.post(self.contextPath + '/table/start_profiling/', data, function(result) {
                        if (self.currentTableName != tableInfo.tableName) {
                            return;
                        }
                        if (result.status == 'ok') {
                            self.refreshInfo(tableInfo.tableName);
                        } else {
                            alert(result.msg);
                        }
                    }, 'json');
                });
            }
        }

        $('#divInfo').find('a[details]').click(function() {
            var data = {
                columnId: $(this).attr('details')
            };
            $.getJSON(self.contextPath + '/table/get_column_details/', data, function(columnInfo) {

                if (self.currentTableName != tableInfo.tableName) {
                    return;
                }

                if ($.isEmptyObject(columnInfo)) {
                    alert('Fail to get column details.');
                    return;
                }

                self.renderColumn(tableInfo, columnInfo);
            });
        });

        $('#divTables').hide();
        $('#divInfo').show();
    },

    renderColumn: function(tableInfo, columnInfo) {
        var self = this;

        columnInfo.rowCount = tableInfo.rowCount;

        var typeFlag = parseInt(columnInfo.typeFlag);
        columnInfo.hasNumericStats = (typeFlag & 1) == 1 && !$.isEmptyObject(columnInfo.numericStats);
        columnInfo.hasStringStats = (typeFlag & 2) == 2 && !$.isEmptyObject(columnInfo.stringStats);
        columnInfo.hasDatetimeStats = (typeFlag & 4) == 4 && !$.isEmptyObject(columnInfo.datetimeStats);

        $('#dlgDetails h3').text('Column: ' + columnInfo.columnName);
        $('#dlgDetails .modal-body').html(self.tplDetails(columnInfo));

        $.each(['numeric', 'string', 'datetime'], function() {

            if (!columnInfo['has' + self.capitalize(this) + 'Stats']) {
                return;
            }

            var stats = columnInfo[this + 'Stats'];

            var top10 = stats.top10;
            var data = [];
            var sum = 0;
            for (var i = 0; i < top10.length; i += 2) {
                data.push([top10[i], top10[i + 1]]);
                sum += top10[i + 1];
            }
            data.push({
                name: 'Other',
                y: columnInfo.rowCount - sum,
                color: 'silver'
            });
            stats.top10Data = data;

            $('#dlgDetails #div' + self.capitalize(this) + 'Top10').highcharts({
                title: {
                    text: 'Top 10 Values'
                },
                tooltip:  {
                    enabled: false
                },
                credits: {
                    enabled: false
                },
                plotOptions: {
                    pie: {
                        dataLabels: {
                            enabled: true,
                            format: '<b>{point.name}</b>: {point.percentage:.2f} %'
                        }
                    }
                },
                series: [{
                    type: 'pie',
                    data: stats.top10Data
                }]
            });

            var bottom10 = stats.bottom10;
            var arr = [];
            for (var i = 0; i < bottom10.length; i += 2) {
                arr.push('<b>' + bottom10[i] + '</b>: ' + bottom10[i + 1]);
            }
            $('#dlgDetails #div' + self.capitalize(this) + 'Bottom10').html(arr.join(', '));
        });

        $('#dlgDetails').modal('show');
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

        $('#divTables').show();
        $('#divInfo').hide();

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

    capitalize: function(s) {
        return s.substr(0, 1).toUpperCase() + s.substr(1);
    },

    _theEnd: undefined
};
