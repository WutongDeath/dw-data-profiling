<%@ tag pageEncoding="UTF-8"%>
<div class="row-fluid">
  <div class="span2 well" style="padding: 10px 0 10px 0;">
    <ul class="nav nav-list">
      <li><a href="/server/list" style="font-weight: bold; color: gray;">Servers</a></li>
      <li><a href="#"><i class="icon-folder-open"></i> dw-slave</a></li>
      <li class="active"><a href="/table/list/2"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_db</a></li>
      <li><a href="#"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_stage</a></li>
      <li><a href="#"><i class="icon-blank"></i> <i class="icon-hdd"></i> dw_extract</a></li>
      <li><a href="#"><i class="icon-folder-close"></i> haozu-etl</a></li>
    </ul>
  </div>

  <div class="span10">
    <jsp:doBody/>
  </div>
</div>
