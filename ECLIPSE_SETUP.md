# Eclipse IDE Setup & Import Guide

This guide ensures that the Globally Scalable Food Delivery Platform is imported into Eclipse IDE without any compile errors or classpath configuration issues.

---

## Prerequisites: Lombok Setup in Eclipse

Since the project uses **Project Lombok** (`@Getter`, `@Setter`, `@Builder`, etc.), Eclipse JDT requires the Lombok agent to compile the java files successfully.

1. Download the Lombok jar from [projectlombok.org/download](https://projectlombok.org/download) (or use the lombok jar in your local maven cache under `~/.m2/repository/org/projectlombok/lombok/1.18.34/lombok-1.18.34.jar`).
2. Run the jar by double-clicking it (or run `java -jar lombok.jar` in your terminal).
3. The Lombok installer will launch. Click **Specify Location...** and select your `eclipse.exe` executable inside your Eclipse installation folder.
4. Click **Install / Update**.
5. Restart your Eclipse IDE.

---

## Step 1: Import the Root Gradle Multi-Module Project

The primary FAANG-level platform uses a multi-module Gradle structure.

1. Open Eclipse IDE.
2. Select **File** -> **Import...**
3. Choose **Gradle** -> **Existing Gradle Project** and click **Next**.
4. Set the **Project root directory** to the workspace folder (where `settings.gradle.kts` lives).
5. Click **Next** -> **Next** -> **Finish**.
6. Eclipse Buildship will automatically read `settings.gradle.kts`, create `.project` / `.classpath` containers for all 30+ service modules, and configure dependencies.

---

## Step 2: Import the Backend (Spring Boot Maven Project)

The `backend` folder contains a separate, standalone Spring Boot Maven project.

1. Select **File** -> **Import...**
2. Choose **Maven** -> **Existing Maven Projects** and click **Next**.
3. Set the **Root Directory** to `workspace_folder/backend` (where `pom.xml` lives).
4. Select the `pom.xml` checkbox.
5. Click **Finish**.
6. M2Eclipse will automatically resolve all Spring Boot dependencies and build the target outputs cleanly.

---

## Troubleshooting Build Issues

- **Compiler Compliance Level**: If you see compilation warnings, ensure Eclipse is using **Java 17** (or above) as the JRE System Library.
- **Project Rebuild**: If Eclipse shows red errors on load, select **Project** -> **Clean...** -> **Clean all projects** to trigger a full JDT compilation pass.
- **Refresh Gradle/Maven**:
  - For Gradle: Right-click the root project -> **Gradle** -> **Refresh Gradle Project**.
  - For Maven: Right-click `food-delivery-backend` -> **Maven** -> **Update Project...**.
