<!-- Modal -->
<div class="modal fade" id="more_info_modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">        
        <h4 class="modal-title" id="more_info_modal_label">More information...</h4>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
      </div>
      
        <div class="modal-body">          
            
            <h4 id="more_info_header"></h4>
            
            <p id="modal_eusig" class="hide">
            This is a threshold value. If the physiological read-out traverses this value then it indicates the possible start or end of a EUSIG event. 
            <br/><br/>This value must be held down for a minimum time for the actual start/end to be confirmed.
            </p>
            
            <p id="modal_holddown" class="hide">
            This is the time for which the physiological value must be above/below the threshold to indicate the unambiguous start/end of a EUSIG event.
            </p>
            
            <p id="modal_time_window" class="hide">
            This is a time period after the beginning of a EUSIG event. Any treatment annotation occurring inside that window will be associated with that event.
            </p>
            
            <p id="modal_event_id" class="hide">
            This is the specific identifier of a single event within a patient's physiological read-out.
            </p>
            
            <p id="modal_adherence_aspect" class="hide">
            This is the aspect of the guideline adherence that will be shown in the box-plot distribution.
            <br/><br/>There are four options: non-adherence degree, duration, (degree / duration) and (degree * duration).
            </p>
            
            <p id="modal_remove_default" class="hide">
            The 'default' state of non-adherence is a large period that appears in every patient's adherence output.
            It represents the period where a patient requires clinical intervention but has not received it.
            <br/><br/>The option to remove this instance is available so that more detailed information about non-adherence distribution
            can be viewed without being skewed by this large data-point.
            </p>
            
        </div>
      
        <div class="modal-footer">                    
            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        </div>
      
    </div>
  </div>
</div>
