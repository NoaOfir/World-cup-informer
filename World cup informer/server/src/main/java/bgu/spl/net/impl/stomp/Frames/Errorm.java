package bgu.spl.net.impl.stomp.Frames;



public class Errorm {
   private String receiptId;
   private String message;
   private String originalMsg;
   private String details;
   private String result;
   
  public Errorm(String receiptId,String message, String originalMsg,String details) {
   this.receiptId=receiptId;
 this.message=message;
   this.originalMsg=originalMsg;
  this.details=details;


   

  }
  public void headerError(){
   result=("ERROR\n"+
   "receipt-id:"+receiptId+"\n"+
   "message:"+message+"\n\n"+
   "The message:\n"+
   "-----\n"+
   originalMsg.substring(0, originalMsg.length()-2)+"\n"+//without the null
    "-----\n"+
    "Did not contain a "+details+" header, which is REQUIRED for message propagation\n"+
    "\u0000");


  }

   public void regError(){
   result=("ERROR\n"+
   "receipt-id:"+receiptId+"\n"+
   "message:"+message+"\n\n"+
   "The message:\n"+
   "-----\n"+
   originalMsg.substring(0, originalMsg.length()-2)+"\n"+//without the null
    "-----\n"+
   details+"\n"+
    "\u0000");


  }

  public String getError(){
    return result;
  }


}
