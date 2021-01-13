# posts service

## Endpoints

* `GET` **/api/posts/{id}**

  **Request Content:**
  
      {}

  **Required:**
  
   * id=[long] `PATH VAR`
     
   **Success Response:**
   
   * Returns the post
   
      **Status Code:** 200 OK
   
      **Response Format:** 

        ```JSON
          {
            "id": 1,
            "title": "title of post 1",
            "body": "body of post 1"
          }
        ```
        
   **Error Response:**
   
   * Returns error response if the given id does not exist in the database
   
      **Status Code:** 404 NOT FOUND
   
      **Response Format:** 

        ```JSON
          {
            
          }
        ```
        
    

## Events

## Dependencies
