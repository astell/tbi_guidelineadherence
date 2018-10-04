<!-- Modal -->
<div class="modal fade" id="login_modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">        
        <h4 class="modal-title" id="login_modal_label">Please login to ICU Chart</h4>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      </div>
      <form action="./checklogin.jsp" method="POST">
        <div class="modal-body">          
            <jsp:include page="/login.jsp" />      
        </div>
        <div class="modal-footer">        
            <button type="submit" class="btn btn-primary">Login</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
        </div>
      </form>
    </div>
  </div>
</div>
