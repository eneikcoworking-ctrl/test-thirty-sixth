# ADR-001: Telegram Core Integration Approach

## Status
Accepted

## Context
The LeadGen Bot project requires integrating with Telegram Core to manage account sessions, send messages, and monitor health as part of the `FEAT-ACC` and `FEAT-CMP` modules.
The two primary options for interacting with Telegram's MTProto protocol are:
1. **Java JNI/JNA Bindings for TDLib:** Running TDLib directly inside or alongside our Java 21 Spring Boot backend.
2. **Dedicated Microservice:** A separate service using a mature, high-level client library (e.g., Python with Pyrogram or Node.js with GramJS) that communicates with our Spring Boot backend via gRPC/REST.

## Trade-offs

### Option 1: Java JNI/JNA with TDLib
* **Pros:**
    * Single codebase and process.
    * Potentially lower latency (no network hops for IPC).
* **Cons:**
    * Native crashes in TDLib can bring down the entire Spring Boot JVM.
    * TDLib requires compiling C++ for the target architecture, adding complexity to the build and deployment pipeline.
    * Handling multiple Telegram sessions concurrently via JNI can introduce threading and memory management complexities.

### Option 2: Python Microservice with Pyrogram
* **Pros:**
    * **Process Isolation:** Native crashes or memory leaks in the Telegram client do not affect the main Spring Boot application.
    * **Ease of Use:** Pyrogram is highly mature, actively maintained, and provides a pythonic interface to Telegram's MTProto.
    * **Scalability:** The Python microservices can be scaled independently of the main backend (e.g., scaling workers per session or group of sessions).
    * No need to manage complex C++ builds; dependencies are easily managed via `pip`.
* **Cons:**
    * Requires setting up a secondary service and defining an API (REST/gRPC) bridge.
    * Slight latency overhead due to inter-process communication.

## Decision
We will use **Option 2: Dedicated Microservice (Python with Pyrogram)** for the Telegram Core Integration.

The isolation of the MTProto session logic from our core business logic in Spring Boot is paramount to ensure the stability of the SaaS platform. The minor IPC overhead is acceptable for cold outreach automation where operations are asynchronous and rate-limited. This approach ensures our JVM remains stable and simplifies deployment.

## Consequences
* We need to define a clear API (REST or gRPC) for the Spring Boot backend to command the Python microservice.
* We must manage deployment for both Java and Python services.
* The next step for backend developers (or the relevant role) is to design and implement this inter-service API bridge.
