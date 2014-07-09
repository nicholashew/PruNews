package com.prudential.objects; 

/** 
 * Asponte 
 * @author Pete Raleigh 
 */ 
public class MyActionsResponse { 
        private boolean success; 
        private String response; 
        private String groupTransId; 
        private int responseCode; 

        public MyActionsResponse() { 
                success = false; 
                groupTransId = null; 
                response = ""; 
                responseCode = 500; 
        } 

        public boolean isSuccess() { 
                return success; 
        } 

        public void setSuccess(boolean success) { 
                this.success = success; 
        } 

        public String getResponse() { 
                return response; 
        } 

        public void setResponse(String response) { 
                this.response = response; 
        } 

        public String getGroupTransId() { 
                return groupTransId; 
        } 

        public void setGroupTransId(String groupTransId) { 
                this.groupTransId = groupTransId; 
        } 

        public int getResponseCode() { 
                return responseCode; 
        } 

        public void setResponseCode(int responseCode) { 
                this.responseCode = responseCode; 
        } 
}