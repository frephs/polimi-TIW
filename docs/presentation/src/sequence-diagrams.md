# Sequence Diagrams

## Generic GET Request Sequence Diagram (HTML Pure)

```mermaid
sequenceDiagram
    actor Client
    participant Filter as Jakarta Filter
    participant Servlet as Child Servlet
    participant controller as Parent Servlet
    participant DAO
    participant DB as Database

    Client->>Servlet: HTTP GET request
    alt session.user is valid or page is unfiltered
        note over Servlet: If database access is needed
        Servlet->>DAO: databaseAccessObject.queryMethod(args)
        DAO->>DB: Execute SELECT query
        alt DB success
            DB->>DAO: Result set
            DAO->>Servlet: beans data
            Servlet->>controller: super.processTemplate(path)
            controller->>controller: contextAttributes.update()
            controller->>controller: templateEngine.processTemplate(path)
            controller->>controller: contextAttributes.clear()
            controller->>controller: response.setContentType("text/html")
            controller->>controller: response.setStatus(200)
            controller->>Client: Data (HTML)
        else DB/DAO error or missing/invalid params
            DB->>DAO: Error/Exception
            DAO->>Servlet: SQLException/Error
            Servlet->>controller: contextAttributes.setFlash(errorMessage) <br> super.sendRedirect(originalURI)
            controller->>controller: controller.setStatus(303 SEE OTHER)
            note over Servlet: The process template due to <br> the redirect to  the previous <br> page will attach  the error<br> message  to the request.
            controller->>Client: sendRedirect(/originalURI)
            Client ->>Servlet: GET request
            note over Client, Servlet: full get flow omitted
            controller ->> Client: initial page reloaded with Error message. 
        end
    else Session invalid/expired and page is filtered
        Filter->>controller: session.setAttributes("from", originalURI) <br> super.sendRedirect(/account)
        note over Servlet: The request is filtered by a Jakarta filter <br> as the user object is not found in the session. <br> After login, if a from attribute is found in the<br> session,  the user will be redirected to the original page.
        controller->>Client: sendRedirect(/account)
        Client ->>Servlet: GET request
        note over Client, Servlet: rest of the flow omitted

    end
```

---

## Generic POST Request Sequence Diagram (HTML Pure)

```mermaid
sequenceDiagram
    actor Client
    participant Filter as Jakarta Filter
    participant Servlet as Child Servlet
    participant controller as Parent Servlet
    participant DAO
    participant DB as Database

    Client->>Servlet: HTTP POST request
    Servlet->>controller: super.checkRequiredParams(args)
    alt All required params present
        alt Session valid
            Servlet->>DAO: Perform DB operation (insert/update/delete)
            DAO->>DB: Execute INSERT/UPDATE/DELETE
            alt DB success
                DB->>DAO: Success/ID/Result
                DAO->>Servlet: Success/ID/Result
                Servlet->>controller: super.sendRedirect(servletUri)
                controller->>controller: response.setStatus(303 SEE_OTHER) <br> contextAttributes.setFlash("successMessage")
                controller->>Client: sendRedirect(/successURI)
                Client->>Servlet: GET request
                note over Client, controller: GET flow omitted 
                controller ->> Client: response with SUCCESS message

            else DB/DAO error
                DB->>DAO: Error/Exception
                DAO->>Servlet: SQLException/Error
                Servlet->>controller: contextAttributes.setFlash(errorMessage) <br> super.sendRedirect(originalURI)
                note over Servlet: Original URI will be reloaded with error message 
                controller->>Client: sendRedirect(/originalURI)
                 Client->>Servlet: GET request
                note over Client, controller: GET flow omitted 
                controller ->> Client: response with ERROR/WARNING message

            end
        else Session invalid/expired
            Filter->>controller: session.setAttribute("from", originalURI) <br> super.sendRedirect(/account)
            controller->>controller: response.setStatus(303 SEE_OTHER)
            note over Servlet: The request is filtered by a Jakarta <br> filter as the user object is not found in the session. <br> After login, if a from attribute is found in the session, <br> the user will be redirected to the original page.
            controller->>Client: sendRedirect(/account)
            Client->>Servlet: GET request
            note over Client, controller: GET flow omitted 
            controller ->> Client: response with REDIRECT INFO message
        end
    else Missing/invalid params
        Servlet->>controller: contextAttributes.setFlash(errorMessage) <br> super.sendRedirect(originalURI)
        controller->>controller: response.setStatus(400 BAD_REQUEST)
        note over Servlet: Original URI will be reloaded with ERROR message 
        Client->>Servlet: GET request
        note over Client, controller: GET flow omitted 
        controller ->> Client: response with ERROR message

        controller->>Client: sendRedirect(/originalURI)
    end
```

---

## Generic GET Request Sequence Diagram (RIA version)

```mermaid
sequenceDiagram
    actor Client
    participant script as Controller Layer
    participant AJAX Layer as AJAX Layer
    participant Servlet as Child Servlet
    participant controller as Parent Servlet
    participant DAO
    participant DB as Database

    Client-->>AJAX Layer: Initiate GET (e.g. fetch data)
    alt Session valid and controller loaded as usual  
    AJAX Layer-->>Servlet: requests.makeXHRequest(uri)
        Servlet->>DAO: Fetch data
        DAO->>DB: Execute SELECT query
        alt DB success
            DB->>DAO: Result set
            DAO->>Servlet: Data
            Servlet-->>AJAX Layer: 200 + response (JSON)
            AJAX Layer-->>script: processXHResponse(response)
            script -->> Client: routing.controllerSwitcher(JSON.parse(response)) <br>components.generateDynamicSection(response.page, response), <br>utils.showPage(response.page)
        else DB/DAO error
            DB->>DAO: Error/Exception
            DAO->>Servlet: SQLException/Error
            Servlet-->>AJAX Layer: JSON error message
            AJAX Layer-->>script: processXHResponse(response)
            script-->>Client: utils.showError(data.error)
        end
    end
```

---

## Generic POST Request Sequence Diagram (RIA version)

```mermaid
sequenceDiagram
    actor Client
    participant script as Controller Layer
    participant AJAX Layer as AJAX Layer

    participant Servlet as Child Servlet
    participant controller as Parent Servlet
    participant DAO
    participant DB as Database

    Client-->>AJAX Layer: Submit POST
    AJAX Layer-->>Servlet: requests.makeXHRequest(newURI, "POST", data)
    Servlet->>controller: super.checkRequiredParams(args)
    alt All required params present
        
            Servlet->>DAO: Perform DB operation (insert/update/delete)
            DAO->>DB: Execute INSERT/UPDATE/DELETE
            alt DB success
                DB->>DAO: Success/ID/Result
                DAO->>Servlet: Success/ID/Result
                note over Servlet, AJAX Layer: Next GET request will attach success message
                Servlet-->>AJAX Layer: 303 See Other to update the page <br> content with the latest data
                AJAX Layer-->>Servlet: requests.makeXHRequest(newURI, "GET")
                note over AJAX Layer, Servlet: Rest of the flow omitted.
            else DB/DAO error
                DB->>DAO: Error/Exception
                DAO->>Servlet: SQLException/Error
                Servlet-->>AJAX Layer: JSON error message
                AJAX Layer-->>script: parseXHResponse(response)
                script-->>Client: utils.showError
            end
    else Missing/invalid params
        controller-->>Servlet: Exception
        Servlet-->>AJAX Layer: 400 Bad Request + JSON error message
        AJAX Layer-->>script: parseXHResponse(response)
        script-->>Client: utils.showError
    end
```
