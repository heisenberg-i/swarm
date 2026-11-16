# PURPOSE: Inventory of retained non-NOVA SWARM source, config, and infrastructure files.

| File | Purpose |
|---|---|
| `.env.example` | Documents required Compose environment variables without secrets. |
| `Dockerfile` | Root Railway/frontend image when the service build context is the repo root. |
| `README.md` | SWARM project file: README.md |
| `backend/.env.example` | SWARM project file: .env.example |
| `backend/Dockerfile` | Multi-stage production image for the SWARM Spring Boot API. |
| `backend/pom.xml` | SWARM project file: pom.xml |
| `backend/railway.toml` | SWARM project file: railway.toml |
| `backend/src/main/java/com/biopower/BiopowerApplication.java` | SWARM project file: BiopowerApplication.java |
| `backend/src/main/java/com/biopower/config/BootstrapAdminInitializer.java` | Creates the first Control Center admin from environment variables. |
| `backend/src/main/java/com/biopower/config/CompanyAccessMigrator.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/DataInitializer.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/DeploymentRoleConfig.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/DeploymentRoleProperties.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/DeviceBackfillRunner.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/EmpowerSaaSAuthProperties.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/HmiEquipmentSeeder.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/IdentityProperties.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/MqttConfig.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/PartnerIntegrationInitializer.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/SchedulingConfiguration.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/SecurityConfig.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/TataSteelDemoInitializer.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/config/TataSteelSensorSync.java` | Spring Boot configuration/startup component for SWARM. |
| `backend/src/main/java/com/biopower/controller/AuthController.java` | HTTP API controller for SWARM auth endpoints. |
| `backend/src/main/java/com/biopower/controller/ControlCenterController.java` | HTTP API controller for SWARM controlcenter endpoints. |
| `backend/src/main/java/com/biopower/controller/DashboardController.java` | HTTP API controller for SWARM dashboard endpoints. |
| `backend/src/main/java/com/biopower/controller/DeviceController.java` | HTTP API controller for SWARM device endpoints. |
| `backend/src/main/java/com/biopower/controller/FieldDeviceController.java` | HTTP API controller for SWARM fielddevice endpoints. |
| `backend/src/main/java/com/biopower/controller/HealthController.java` | Liveness payload including PostgreSQL connectivity. |
| `backend/src/main/java/com/biopower/controller/HmiController.java` | HTTP API controller for SWARM hmi endpoints. |
| `backend/src/main/java/com/biopower/controller/IoTController.java` | HTTP API controller for SWARM iot endpoints. |
| `backend/src/main/java/com/biopower/controller/MaintenanceController.java` | HTTP API controller for SWARM maintenance endpoints. |
| `backend/src/main/java/com/biopower/controller/NotificationController.java` | HTTP API controller for SWARM notification endpoints. |
| `backend/src/main/java/com/biopower/controller/PartnerAdminController.java` | HTTP API controller for SWARM partneradmin endpoints. |
| `backend/src/main/java/com/biopower/controller/PartnerTelemetryController.java` | HTTP API controller for SWARM partnertelemetry endpoints. |
| `backend/src/main/java/com/biopower/controller/PlantController.java` | HTTP API controller for SWARM plant endpoints. |
| `backend/src/main/java/com/biopower/controller/ReportSettingsController.java` | HTTP API controller for SWARM reportsettings endpoints. |
| `backend/src/main/java/com/biopower/controller/SensorNodeController.java` | HTTP API controller for SWARM sensornode endpoints. |
| `backend/src/main/java/com/biopower/controller/UserController.java` | HTTP API controller for SWARM user endpoints. |
| `backend/src/main/java/com/biopower/dto/partner/PartnerAlertResponse.java` | Request/response DTO for SWARM API payloads (PartnerAlertResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerDailyAggregatePageResponse.java` | Request/response DTO for SWARM API payloads (PartnerDailyAggregatePageResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerDailyAggregateResponse.java` | Request/response DTO for SWARM API payloads (PartnerDailyAggregateResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerDeviceResponse.java` | Request/response DTO for SWARM API payloads (PartnerDeviceResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerHealthResponse.java` | Request/response DTO for SWARM API payloads (PartnerHealthResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerHistoryPageResponse.java` | Request/response DTO for SWARM API payloads (PartnerHistoryPageResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerPlantHealthResponse.java` | Request/response DTO for SWARM API payloads (PartnerPlantHealthResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerPlantResponse.java` | Request/response DTO for SWARM API payloads (PartnerPlantResponse). |
| `backend/src/main/java/com/biopower/dto/partner/PartnerReadingResponse.java` | Request/response DTO for SWARM API payloads (PartnerReadingResponse). |
| `backend/src/main/java/com/biopower/dto/request/ApproveUserRequest.java` | Request/response DTO for SWARM API payloads (ApproveUserRequest). |
| `backend/src/main/java/com/biopower/dto/request/AssignPlantsRequest.java` | Request/response DTO for SWARM API payloads (AssignPlantsRequest). |
| `backend/src/main/java/com/biopower/dto/request/ChangePasswordRequest.java` | Request/response DTO for SWARM API payloads (ChangePasswordRequest). |
| `backend/src/main/java/com/biopower/dto/request/CompanyRequest.java` | Request/response DTO for SWARM API payloads (CompanyRequest). |
| `backend/src/main/java/com/biopower/dto/request/DeviceChannelRequest.java` | Request/response DTO for SWARM API payloads (DeviceChannelRequest). |
| `backend/src/main/java/com/biopower/dto/request/DevicePairRequest.java` | Request/response DTO for SWARM API payloads (DevicePairRequest). |
| `backend/src/main/java/com/biopower/dto/request/EspStatusSnapshot.java` | Request/response DTO for SWARM API payloads (EspStatusSnapshot). |
| `backend/src/main/java/com/biopower/dto/request/ForgotPasswordRequest.java` | Request/response DTO for SWARM API payloads (ForgotPasswordRequest). |
| `backend/src/main/java/com/biopower/dto/request/HmiCommandRequest.java` | Request/response DTO for SWARM API payloads (HmiCommandRequest). |
| `backend/src/main/java/com/biopower/dto/request/HmiEnabledRequest.java` | Request/response DTO for SWARM API payloads (HmiEnabledRequest). |
| `backend/src/main/java/com/biopower/dto/request/HmiMasterRequest.java` | Request/response DTO for SWARM API payloads (HmiMasterRequest). |
| `backend/src/main/java/com/biopower/dto/request/IoTBatchRequest.java` | Request/response DTO for SWARM API payloads (IoTBatchRequest). |
| `backend/src/main/java/com/biopower/dto/request/IoTDataRequest.java` | Request/response DTO for SWARM API payloads (IoTDataRequest). |
| `backend/src/main/java/com/biopower/dto/request/LoginRequest.java` | Request/response DTO for SWARM API payloads (LoginRequest). |
| `backend/src/main/java/com/biopower/dto/request/PartnerApiKeyRequest.java` | Request/response DTO for SWARM API payloads (PartnerApiKeyRequest). |
| `backend/src/main/java/com/biopower/dto/request/PartnerOrganizationRequest.java` | Request/response DTO for SWARM API payloads (PartnerOrganizationRequest). |
| `backend/src/main/java/com/biopower/dto/request/PlantRequest.java` | Request/response DTO for SWARM API payloads (PlantRequest). |
| `backend/src/main/java/com/biopower/dto/request/ReportRequest.java` | Request/response DTO for SWARM API payloads (ReportRequest). |
| `backend/src/main/java/com/biopower/dto/request/SensorNodeRequest.java` | Request/response DTO for SWARM API payloads (SensorNodeRequest). |
| `backend/src/main/java/com/biopower/dto/request/SettingsRequest.java` | Request/response DTO for SWARM API payloads (SettingsRequest). |
| `backend/src/main/java/com/biopower/dto/request/SignupRequest.java` | Request/response DTO for SWARM API payloads (SignupRequest). |
| `backend/src/main/java/com/biopower/dto/request/SsoCallbackRequest.java` | Request/response DTO for SWARM API payloads (SsoCallbackRequest). |
| `backend/src/main/java/com/biopower/dto/request/SuperAdminRequest.java` | Request/response DTO for SWARM API payloads (SuperAdminRequest). |
| `backend/src/main/java/com/biopower/dto/request/SyncReadingsRequest.java` | Request/response DTO for SWARM API payloads (SyncReadingsRequest). |
| `backend/src/main/java/com/biopower/dto/request/UserRequest.java` | Request/response DTO for SWARM API payloads (UserRequest). |
| `backend/src/main/java/com/biopower/dto/response/AiRecommendationResponse.java` | Request/response DTO for SWARM API payloads (AiRecommendationResponse). |
| `backend/src/main/java/com/biopower/dto/response/AlertResponse.java` | Request/response DTO for SWARM API payloads (AlertResponse). |
| `backend/src/main/java/com/biopower/dto/response/ApiResponse.java` | Request/response DTO for SWARM API payloads (ApiResponse). |
| `backend/src/main/java/com/biopower/dto/response/AuthResponse.java` | Request/response DTO for SWARM API payloads (AuthResponse). |
| `backend/src/main/java/com/biopower/dto/response/CompanyDetailResponse.java` | Request/response DTO for SWARM API payloads (CompanyDetailResponse). |
| `backend/src/main/java/com/biopower/dto/response/CompanyResponse.java` | Request/response DTO for SWARM API payloads (CompanyResponse). |
| `backend/src/main/java/com/biopower/dto/response/ControlCenterOverviewResponse.java` | Request/response DTO for SWARM API payloads (ControlCenterOverviewResponse). |
| `backend/src/main/java/com/biopower/dto/response/ControlCenterSensorNodeResponse.java` | Request/response DTO for SWARM API payloads (ControlCenterSensorNodeResponse). |
| `backend/src/main/java/com/biopower/dto/response/ControlCenterUserResponse.java` | Request/response DTO for SWARM API payloads (ControlCenterUserResponse). |
| `backend/src/main/java/com/biopower/dto/response/DashboardResponse.java` | Request/response DTO for SWARM API payloads (DashboardResponse). |
| `backend/src/main/java/com/biopower/dto/response/DevicePairResponse.java` | Request/response DTO for SWARM API payloads (DevicePairResponse). |
| `backend/src/main/java/com/biopower/dto/response/FieldDeviceChannelResponse.java` | Request/response DTO for SWARM API payloads (FieldDeviceChannelResponse). |
| `backend/src/main/java/com/biopower/dto/response/FieldDeviceResponse.java` | Request/response DTO for SWARM API payloads (FieldDeviceResponse). |
| `backend/src/main/java/com/biopower/dto/response/HmiDiagramResponse.java` | Request/response DTO for SWARM API payloads (HmiDiagramResponse). |
| `backend/src/main/java/com/biopower/dto/response/HmiEquipmentStateResponse.java` | Request/response DTO for SWARM API payloads (HmiEquipmentStateResponse). |
| `backend/src/main/java/com/biopower/dto/response/HmiHotspotResponse.java` | Request/response DTO for SWARM API payloads (HmiHotspotResponse). |
| `backend/src/main/java/com/biopower/dto/response/HmiStateResponse.java` | Request/response DTO for SWARM API payloads (HmiStateResponse). |
| `backend/src/main/java/com/biopower/dto/response/PairedDeviceResponse.java` | Request/response DTO for SWARM API payloads (PairedDeviceResponse). |
| `backend/src/main/java/com/biopower/dto/response/PartnerApiKeyCreatedResponse.java` | Request/response DTO for SWARM API payloads (PartnerApiKeyCreatedResponse). |
| `backend/src/main/java/com/biopower/dto/response/PartnerApiKeyResponse.java` | Request/response DTO for SWARM API payloads (PartnerApiKeyResponse). |
| `backend/src/main/java/com/biopower/dto/response/PartnerOrganizationResponse.java` | Request/response DTO for SWARM API payloads (PartnerOrganizationResponse). |
| `backend/src/main/java/com/biopower/dto/response/PlantResponse.java` | Request/response DTO for SWARM API payloads (PlantResponse). |
| `backend/src/main/java/com/biopower/dto/response/PredictiveMaintenanceResponse.java` | Request/response DTO for SWARM API payloads (PredictiveMaintenanceResponse). |
| `backend/src/main/java/com/biopower/dto/response/SaaSIdentityProfile.java` | Request/response DTO for SWARM API payloads (SaaSIdentityProfile). |
| `backend/src/main/java/com/biopower/dto/response/SensorNodeResponse.java` | Request/response DTO for SWARM API payloads (SensorNodeResponse). |
| `backend/src/main/java/com/biopower/dto/response/SensorReadingResponse.java` | Request/response DTO for SWARM API payloads (SensorReadingResponse). |
| `backend/src/main/java/com/biopower/dto/response/SsoConfigResponse.java` | Request/response DTO for SWARM API payloads (SsoConfigResponse). |
| `backend/src/main/java/com/biopower/dto/response/UserResponse.java` | Request/response DTO for SWARM API payloads (UserResponse). |
| `backend/src/main/java/com/biopower/exception/BadRequestException.java` | API exception type for SWARM error handling. |
| `backend/src/main/java/com/biopower/exception/GlobalExceptionHandler.java` | Maps exceptions to API errors without leaking stack traces. |
| `backend/src/main/java/com/biopower/exception/ResourceNotFoundException.java` | API exception type for SWARM error handling. |
| `backend/src/main/java/com/biopower/exception/UnauthorizedException.java` | API exception type for SWARM error handling. |
| `backend/src/main/java/com/biopower/model/entity/AiRecommendation.java` | JPA entity mapping for SWARM table/fields used by AiRecommendation. |
| `backend/src/main/java/com/biopower/model/entity/Alert.java` | JPA entity mapping for SWARM table/fields used by Alert. |
| `backend/src/main/java/com/biopower/model/entity/Company.java` | JPA entity mapping for SWARM table/fields used by Company. |
| `backend/src/main/java/com/biopower/model/entity/Device.java` | JPA entity mapping for SWARM table/fields used by Device. |
| `backend/src/main/java/com/biopower/model/entity/DeviceChannel.java` | JPA entity mapping for SWARM table/fields used by DeviceChannel. |
| `backend/src/main/java/com/biopower/model/entity/Equipment.java` | JPA entity mapping for SWARM table/fields used by Equipment. |
| `backend/src/main/java/com/biopower/model/entity/HmiEquipment.java` | JPA entity mapping for SWARM table/fields used by HmiEquipment. |
| `backend/src/main/java/com/biopower/model/entity/HmiEquipmentState.java` | JPA entity mapping for SWARM table/fields used by HmiEquipmentState. |
| `backend/src/main/java/com/biopower/model/entity/HmiPlantState.java` | JPA entity mapping for SWARM table/fields used by HmiPlantState. |
| `backend/src/main/java/com/biopower/model/entity/IngestRejected.java` | JPA entity mapping for SWARM table/fields used by IngestRejected. |
| `backend/src/main/java/com/biopower/model/entity/PartnerApiKey.java` | JPA entity mapping for SWARM table/fields used by PartnerApiKey. |
| `backend/src/main/java/com/biopower/model/entity/PartnerOrganization.java` | JPA entity mapping for SWARM table/fields used by PartnerOrganization. |
| `backend/src/main/java/com/biopower/model/entity/Plant.java` | JPA entity mapping for SWARM table/fields used by Plant. |
| `backend/src/main/java/com/biopower/model/entity/PredictiveMaintenance.java` | JPA entity mapping for SWARM table/fields used by PredictiveMaintenance. |
| `backend/src/main/java/com/biopower/model/entity/ProcessArea.java` | JPA entity mapping for SWARM table/fields used by ProcessArea. |
| `backend/src/main/java/com/biopower/model/entity/Report.java` | JPA entity mapping for SWARM table/fields used by Report. |
| `backend/src/main/java/com/biopower/model/entity/SensorMessage.java` | JPA entity mapping for SWARM table/fields used by SensorMessage. |
| `backend/src/main/java/com/biopower/model/entity/SensorNode.java` | JPA entity mapping for SWARM table/fields used by SensorNode. |
| `backend/src/main/java/com/biopower/model/entity/SensorReading.java` | JPA entity mapping for SWARM table/fields used by SensorReading. |
| `backend/src/main/java/com/biopower/model/entity/SensorReadingId.java` | JPA entity mapping for SWARM table/fields used by SensorReadingId. |
| `backend/src/main/java/com/biopower/model/entity/SystemSettings.java` | JPA entity mapping for SWARM table/fields used by SystemSettings. |
| `backend/src/main/java/com/biopower/model/entity/User.java` | JPA entity mapping for SWARM table/fields used by User. |
| `backend/src/main/java/com/biopower/model/enums/AiIssueType.java` | Enumerated value used by SWARM AiIssueType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/AlertSeverity.java` | Enumerated value used by SWARM AlertSeverity domain logic. |
| `backend/src/main/java/com/biopower/model/enums/AlertStatus.java` | Enumerated value used by SWARM AlertStatus domain logic. |
| `backend/src/main/java/com/biopower/model/enums/CompanyStatus.java` | Enumerated value used by SWARM CompanyStatus domain logic. |
| `backend/src/main/java/com/biopower/model/enums/DataQuality.java` | Enumerated value used by SWARM DataQuality domain logic. |
| `backend/src/main/java/com/biopower/model/enums/DeviceKind.java` | Enumerated value used by SWARM DeviceKind domain logic. |
| `backend/src/main/java/com/biopower/model/enums/DeviceLifecycle.java` | Enumerated value used by SWARM DeviceLifecycle domain logic. |
| `backend/src/main/java/com/biopower/model/enums/EquipmentType.java` | Enumerated value used by SWARM EquipmentType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/HealthStatus.java` | Enumerated value used by SWARM HealthStatus domain logic. |
| `backend/src/main/java/com/biopower/model/enums/HmiControlMode.java` | Enumerated value used by SWARM HmiControlMode domain logic. |
| `backend/src/main/java/com/biopower/model/enums/HmiEquipmentKind.java` | Enumerated value used by SWARM HmiEquipmentKind domain logic. |
| `backend/src/main/java/com/biopower/model/enums/HmiZone.java` | Enumerated value used by SWARM HmiZone domain logic. |
| `backend/src/main/java/com/biopower/model/enums/IngestTransport.java` | Enumerated value used by SWARM IngestTransport domain logic. |
| `backend/src/main/java/com/biopower/model/enums/NodeStatus.java` | Enumerated value used by SWARM NodeStatus domain logic. |
| `backend/src/main/java/com/biopower/model/enums/PlantStatus.java` | Enumerated value used by SWARM PlantStatus domain logic. |
| `backend/src/main/java/com/biopower/model/enums/PlantType.java` | Enumerated value used by SWARM PlantType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/ReadingQuality.java` | Enumerated value used by SWARM ReadingQuality domain logic. |
| `backend/src/main/java/com/biopower/model/enums/ReadingType.java` | Enumerated value used by SWARM ReadingType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/ReportType.java` | Enumerated value used by SWARM ReportType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/SensorType.java` | Enumerated value used by SWARM SensorType domain logic. |
| `backend/src/main/java/com/biopower/model/enums/UserRole.java` | Enumerated value used by SWARM UserRole domain logic. |
| `backend/src/main/java/com/biopower/model/enums/UserStatus.java` | Enumerated value used by SWARM UserStatus domain logic. |
| `backend/src/main/java/com/biopower/mqtt/MqttConnectionHealth.java` | MQTT ingest/health adapter for SWARM device telemetry. |
| `backend/src/main/java/com/biopower/mqtt/MqttTelemetryHandler.java` | MQTT ingest/health adapter for SWARM device telemetry. |
| `backend/src/main/java/com/biopower/repository/AiRecommendationRepository.java` | Spring Data access for SWARM AiRecommendation persistence. |
| `backend/src/main/java/com/biopower/repository/AlertRepository.java` | Spring Data access for SWARM Alert persistence. |
| `backend/src/main/java/com/biopower/repository/CompanyRepository.java` | Spring Data access for SWARM Company persistence. |
| `backend/src/main/java/com/biopower/repository/DeviceChannelRepository.java` | Spring Data access for SWARM DeviceChannel persistence. |
| `backend/src/main/java/com/biopower/repository/DeviceRepository.java` | Spring Data access for SWARM Device persistence. |
| `backend/src/main/java/com/biopower/repository/EquipmentRepository.java` | Spring Data access for SWARM Equipment persistence. |
| `backend/src/main/java/com/biopower/repository/HmiEquipmentRepository.java` | Spring Data access for SWARM HmiEquipment persistence. |
| `backend/src/main/java/com/biopower/repository/HmiEquipmentStateRepository.java` | Spring Data access for SWARM HmiEquipmentState persistence. |
| `backend/src/main/java/com/biopower/repository/HmiPlantStateRepository.java` | Spring Data access for SWARM HmiPlantState persistence. |
| `backend/src/main/java/com/biopower/repository/IngestRejectedRepository.java` | Spring Data access for SWARM IngestRejected persistence. |
| `backend/src/main/java/com/biopower/repository/PartnerApiKeyRepository.java` | Spring Data access for SWARM PartnerApiKey persistence. |
| `backend/src/main/java/com/biopower/repository/PartnerOrganizationRepository.java` | Spring Data access for SWARM PartnerOrganization persistence. |
| `backend/src/main/java/com/biopower/repository/PlantRepository.java` | Spring Data access for SWARM Plant persistence. |
| `backend/src/main/java/com/biopower/repository/PredictiveMaintenanceRepository.java` | Spring Data access for SWARM PredictiveMaintenance persistence. |
| `backend/src/main/java/com/biopower/repository/ProcessAreaRepository.java` | Spring Data access for SWARM ProcessArea persistence. |
| `backend/src/main/java/com/biopower/repository/ReportRepository.java` | Spring Data access for SWARM Report persistence. |
| `backend/src/main/java/com/biopower/repository/SensorMessageRepository.java` | Spring Data access for SWARM SensorMessage persistence. |
| `backend/src/main/java/com/biopower/repository/SensorNodeRepository.java` | Spring Data access for SWARM SensorNode persistence. |
| `backend/src/main/java/com/biopower/repository/SensorReadingRepository.java` | Spring Data access for SWARM SensorReading persistence. |
| `backend/src/main/java/com/biopower/repository/SensorReadingRepositoryCustom.java` | Spring Data access for SWARM SensorReadingCustom persistence. |
| `backend/src/main/java/com/biopower/repository/SensorReadingRepositoryImpl.java` | Spring Data access for SWARM SensorReading persistence. |
| `backend/src/main/java/com/biopower/repository/SystemSettingsRepository.java` | Spring Data access for SWARM SystemSettings persistence. |
| `backend/src/main/java/com/biopower/repository/UserRepository.java` | Spring Data access for SWARM User persistence. |
| `backend/src/main/java/com/biopower/security/ApiKeyAuthenticationFilter.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/CustomAccessDeniedHandler.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/CustomUserDetailsService.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/DeviceAuthenticationFilter.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/DevicePrincipal.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/IngestRateLimitFilter.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/JwtAuthenticationEntryPoint.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/JwtAuthenticationFilter.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/JwtTokenProvider.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/PartnerPrincipal.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/security/UserPrincipal.java` | Authentication/authorization component for SWARM API security. |
| `backend/src/main/java/com/biopower/service/AiHealthService.java` | Application service implementing SWARM aihealth business rules. |
| `backend/src/main/java/com/biopower/service/AlertService.java` | Application service implementing SWARM alert business rules. |
| `backend/src/main/java/com/biopower/service/AnalyticsService.java` | Application service implementing SWARM analytics business rules. |
| `backend/src/main/java/com/biopower/service/AuthService.java` | Application service implementing SWARM auth business rules. |
| `backend/src/main/java/com/biopower/service/ControlCenterService.java` | Application service implementing SWARM controlcenter business rules. |
| `backend/src/main/java/com/biopower/service/DataRetentionScheduler.java` | Application service implementing SWARM dataretentionscheduler business rules. |
| `backend/src/main/java/com/biopower/service/DeviceRegistryService.java` | Application service implementing SWARM deviceregistry business rules. |
| `backend/src/main/java/com/biopower/service/DeviceService.java` | Application service implementing SWARM device business rules. |
| `backend/src/main/java/com/biopower/service/EmpowerSaaSAuthClient.java` | Application service implementing SWARM empowersaasauthclient business rules. |
| `backend/src/main/java/com/biopower/service/EspProxyService.java` | Application service implementing SWARM espproxy business rules. |
| `backend/src/main/java/com/biopower/service/FieldDeviceQueryService.java` | Application service implementing SWARM fielddevicequery business rules. |
| `backend/src/main/java/com/biopower/service/HmiPlantSetupService.java` | Application service implementing SWARM hmiplantsetup business rules. |
| `backend/src/main/java/com/biopower/service/HmiService.java` | Application service implementing SWARM hmi business rules. |
| `backend/src/main/java/com/biopower/service/IoTDataService.java` | Application service implementing SWARM iotdata business rules. |
| `backend/src/main/java/com/biopower/service/PartnerApiKeyService.java` | Application service implementing SWARM partnerapikey business rules. |
| `backend/src/main/java/com/biopower/service/PartnerTelemetryService.java` | Application service implementing SWARM partnertelemetry business rules. |
| `backend/src/main/java/com/biopower/service/PlantAccessService.java` | Application service implementing SWARM plantaccess business rules. |
| `backend/src/main/java/com/biopower/service/PlantService.java` | Application service implementing SWARM plant business rules. |
| `backend/src/main/java/com/biopower/service/PredictiveMaintenanceService.java` | Application service implementing SWARM predictivemaintenance business rules. |
| `backend/src/main/java/com/biopower/service/ReportService.java` | Application service implementing SWARM report business rules. |
| `backend/src/main/java/com/biopower/service/SensorNodeService.java` | Application service implementing SWARM sensornode business rules. |
| `backend/src/main/java/com/biopower/service/SettingsService.java` | Application service implementing SWARM settings business rules. |
| `backend/src/main/java/com/biopower/service/SwarmUrlService.java` | Application service implementing SWARM swarmurl business rules. |
| `backend/src/main/java/com/biopower/service/TelemetryIngestService.java` | Application service implementing SWARM telemetryingest business rules. |
| `backend/src/main/java/com/biopower/service/UserService.java` | Application service implementing SWARM user business rules. |
| `backend/src/main/java/com/biopower/telemetry/IngestEnvelope.java` | Telemetry ingest DTO/helper used by SWARM HTTP and MQTT pipelines. |
| `backend/src/main/java/com/biopower/telemetry/IngestPoint.java` | Telemetry ingest DTO/helper used by SWARM HTTP and MQTT pipelines. |
| `backend/src/main/java/com/biopower/telemetry/IngestResult.java` | Telemetry ingest DTO/helper used by SWARM HTTP and MQTT pipelines. |
| `backend/src/main/java/com/biopower/util/ApiKeyHasher.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/java/com/biopower/util/DeviceSecretHasher.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/java/com/biopower/util/ExternalOrgIds.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/java/com/biopower/util/PartnerMetricCatalog.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/java/com/biopower/util/PlantSensorTypes.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/java/com/biopower/util/SensorHeartbeat.java` | Shared backend helper used by SWARM services. |
| `backend/src/main/resources/application-dev.yml` | Local-dev Spring profile (same PostgreSQL, CORS for Vite). |
| `backend/src/main/resources/application-prod.yml` | Production Spring profile used by Docker Compose. |
| `backend/src/main/resources/application-standby.yml` | Standby/failover Spring profile for a secondary API instance. |
| `backend/src/main/resources/application.yml` | Default Spring configuration for PostgreSQL, JWT, MQTT, and CORS. |
| `backend/src/main/resources/db/migration/V1__swarm_app_schema.sql` | Flyway migration for the SWARM PostgreSQL application schema. |
| `backend/src/main/resources/db/migration/V2__seed_app_and_h2_data.sql` | Flyway migration for the SWARM PostgreSQL application schema. |
| `backend/src/test/java/com/biopower/config/DeploymentRolePropertiesTest.java` | Automated test covering SWARM DeploymentRolePropertiesTest. |
| `backend/src/test/java/com/biopower/mqtt/MqttTelemetryHandlerTest.java` | Automated test covering SWARM MqttTelemetryHandlerTest. |
| `backend/src/test/java/com/biopower/security/DeviceAuthIT.java` | Automated test covering SWARM DeviceAuthIT. |
| `backend/src/test/java/com/biopower/security/PartnerPrincipalTest.java` | Automated test covering SWARM PartnerPrincipalTest. |
| `backend/src/test/java/com/biopower/security/Slice0SecurityIT.java` | Automated test covering SWARM Slice0SecurityIT. |
| `backend/src/test/java/com/biopower/service/PartnerTelemetryAllowListTest.java` | Automated test covering SWARM PartnerTelemetryAllowListTest. |
| `backend/src/test/java/com/biopower/service/SwarmUrlServiceTest.java` | Automated test covering SWARM SwarmUrlServiceTest. |
| `backend/src/test/java/com/biopower/telemetry/TelemetryIngestServiceTest.java` | Automated test covering SWARM TelemetryIngestServiceTest. |
| `backend/src/test/java/com/biopower/util/DeviceSecretHasherTest.java` | Automated test covering SWARM DeviceSecretHasherTest. |
| `backend/src/test/java/com/biopower/util/ExternalOrgIdsTest.java` | Automated test covering SWARM ExternalOrgIdsTest. |
| `database/reset-non-nova.sql` | Deletes non-NOVA application rows after backup restore. |
| `database/swarm-core-init.sql` | Creates the four core Timescale tables on a fresh empty database. |
| `database/schema.sql` | Points at Flyway as the canonical SWARM PostgreSQL schema. |
| `docker-compose.yml` | Defines the production SWARM Docker Compose services. |
| `docker/mosquitto/mosquitto.conf` | Mosquitto listener config for optional SWARM MQTT ingest. |
| `docker/postgres/bootstrap.sh` | Initializes empty Timescale core schema or restores a dump once. |
| `docs/ANDROID_APK.md` | Operator/developer documentation for SWARM. |
| `docs/API.md` | Operator/developer documentation for SWARM. |
| `docs/CLOUDFLARE_FAILOVER.md` | Operator/developer documentation for SWARM. |
| `docs/CLOUD_AGENT_HANDOFF_ANDROID.md` | Operator/developer documentation for SWARM. |
| `docs/CONTROL_CENTER.md` | Operator/developer documentation for SWARM. |
| `docs/CONTROL_CENTER_MODULES.md` | Operator/developer documentation for SWARM. |
| `docs/DOCKER.md` | Explains Docker Compose startup, volumes, and host PostgreSQL retirement. |
| `docs/EMPOWER_CENTRALIZED_AUTH.md` | Operator/developer documentation for SWARM. |
| `docs/EMPOWER_HANDOFF.md` | Operator/developer documentation for SWARM. |
| `docs/EMPOWER_SHARED_LOGIN_CONNECTIVITY.md` | Operator/developer documentation for SWARM. |
| `docs/EMPOWER_SWARM_WORKFLOW_ALIGNMENT_REPLY_2026-09-01.md` | Operator/developer documentation for SWARM. |
| `docs/ER_DIAGRAM.md` | Operator/developer documentation for SWARM. |
| `docs/ESP_SENSOR_HUB_MANUAL.md` | Operator/developer documentation for SWARM. |
| `docs/INSTALLATION.md` | Operator/developer documentation for SWARM. |
| `docs/NOVA_SPACE_OP_ARCHITECTURE.md` | Operator/developer documentation for SWARM. |
| `docs/PARTNER_API.md` | Operator/developer documentation for SWARM. |
| `docs/PLANT_HMI.md` | Operator/developer documentation for SWARM. |
| `docs/RAILWAY_COST_OPTIMIZATION.md` | Operator/developer documentation for SWARM. |
| `docs/RAILWAY_HOSTING.md` | Operator/developer documentation for SWARM. |
| `docs/RAILWAY_MANUAL_FAILOVER.md` | Operator/developer documentation for SWARM. |
| `docs/RAILWAY_STANDBY_OPTIMIZATION.md` | Operator/developer documentation for SWARM. |
| `docs/RASPBERRY_PI_DEPLOYMENT.md` | Operator/developer documentation for SWARM. |
| `docs/RPI_FRESH_SETUP_WINSCP.md` | Operator/developer documentation for SWARM. |
| `docs/RPi_PRIMARY_RUNBOOK.md` | Operator/developer documentation for SWARM. |
| `docs/USER_MANUAL.md` | Operator/developer documentation for SWARM. |
| `docs/generate_integration_plan_pdf.py` | Operator/developer documentation for SWARM. |
| `docs/generate_nova_llm_keys_pdf.py` | Operator/developer documentation for SWARM. |
| `docs/generate_user_manual_pdf.py` | Operator/developer documentation for SWARM. |
| `docs/generate_workflow_pdf.py` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/00-executive-vision.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/01-repository-audit.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/02-capability-inventory.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/03-product-requirements.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/04-user-journeys-and-roles.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/05-business-domain-model.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/06-target-system-architecture.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/07-module-architecture.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/08-database-architecture.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/09-authentication-authorization.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/10-threat-model.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/11-tenant-isolation.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/12-api-specification.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/13-sensor-device-model.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/14-telemetry-architecture.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/15-hmi-command-lifecycle.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/16-industrial-safety.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/17-alerts-notifications.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/18-operations-maintenance.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/19-analytics-and-ai.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/20-empower-partner-api.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/21-android-strategy.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/22-deployment-infrastructure.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/23-test-strategy.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/24-data-migration.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/25-implementation-roadmap.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/26-architecture-decision-records.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/27-cursor-implementation-master-prompt.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/28-mqtt-and-telemetry.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/ENGINEERING_REPORT.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/PROTECTED_MODULES.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/README.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/REBUILD_INVENTORY.md` | Operator/developer documentation for SWARM. |
| `docs/swarm-2-architecture/progress.md` | Operator/developer documentation for SWARM. |
| `frontend/.env.production.example` | SWARM project file: .env.production.example |
| `frontend/Dockerfile` | Multi-stage production image for the SWARM React UI. |
| `frontend/android/app/src/androidTest/java/com/getcapacitor/myapp/ExampleInstrumentedTest.java` | SWARM project file: ExampleInstrumentedTest.java |
| `frontend/android/app/src/main/AndroidManifest.xml` | SWARM project file: AndroidManifest.xml |
| `frontend/android/app/src/main/java/com/nanofarm/swarm/MainActivity.java` | SWARM project file: MainActivity.java |
| `frontend/android/app/src/main/res/drawable-v24/ic_launcher_foreground.xml` | SWARM project file: ic_launcher_foreground.xml |
| `frontend/android/app/src/main/res/drawable/ic_launcher_background.xml` | SWARM project file: ic_launcher_background.xml |
| `frontend/android/app/src/main/res/layout/activity_main.xml` | SWARM project file: activity_main.xml |
| `frontend/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | SWARM project file: ic_launcher.xml |
| `frontend/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | SWARM project file: ic_launcher_round.xml |
| `frontend/android/app/src/main/res/values/ic_launcher_background.xml` | SWARM project file: ic_launcher_background.xml |
| `frontend/android/app/src/main/res/values/strings.xml` | SWARM project file: strings.xml |
| `frontend/android/app/src/main/res/values/styles.xml` | SWARM project file: styles.xml |
| `frontend/android/app/src/main/res/xml/file_paths.xml` | SWARM project file: file_paths.xml |
| `frontend/android/app/src/main/res/xml/network_security_config.xml` | SWARM project file: network_security_config.xml |
| `frontend/android/app/src/test/java/com/getcapacitor/myapp/ExampleUnitTest.java` | SWARM project file: ExampleUnitTest.java |
| `frontend/android/gradle.properties` | SWARM project file: gradle.properties |
| `frontend/android/gradle/wrapper/gradle-wrapper.properties` | SWARM project file: gradle-wrapper.properties |
| `frontend/android/local.properties.example` | SWARM project file: local.properties.example |
| `frontend/docker-entrypoint.sh` | Renders nginx listen port and starts the SWARM frontend. |
| `frontend/nginx.conf` | SWARM project file: nginx.conf |
| `frontend/public/hmi/README.md` | SWARM project file: README.md |
| `frontend/public/hmi/swarm-hmi.js` | SWARM project file: swarm-hmi.js |
| `frontend/railway.toml` | SWARM project file: railway.toml |
| `frontend/scripts/android-gradle.js` | SWARM project file: android-gradle.js |
| `frontend/src/App.jsx` | React router for Control Center and plant-app pages (NOVA routes unchanged). |
| `frontend/src/components/common/EmptyState.jsx` | Shared empty-state layout for list and chart pages. |
| `frontend/src/components/common/Logo.jsx` | Reusable React component for the SWARM UI. |
| `frontend/src/components/control-center/ControlCenterLayout.jsx` | Reusable React component for the SWARM UI. |
| `frontend/src/components/hmi/BiogasPlantDiagram.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/HmiAlarmSummary.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/HmiAuditPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/HmiDiagnosticsPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/HmiPageNav.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/HmiTrendsPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/OutputEnablePanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/PlantOverviewPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/SwarmCoreHeader.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/ZoneMonitorPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/EquipmentPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/HmiPageTabs.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/InstrumentationPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/MotorControlCard.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/MotorStatusLegend.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/PidDiagram.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/PidSchematic.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/PlantMasterControls.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/ProcessFlowLegend.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/README.md` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/SafetyStatusBar.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/SwarmControlPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/ZoneCircuitPanel.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/hmi/_quarantine/ZoneEquipmentTable.jsx` | Plant HMI UI component used by the SWARM P&ID screens. |
| `frontend/src/components/layout/MainLayout.jsx` | Reusable React component for the SWARM UI. |
| `frontend/src/context/AuthContext.jsx` | Holds JWT session and role helpers for the React app. |
| `frontend/src/data/_quarantine/README.md` | SWARM project file: README.md |
| `frontend/src/data/_quarantine/tataSteelEquipment.js` | SWARM project file: tataSteelEquipment.js |
| `frontend/src/data/pidRegistry.js` | SWARM project file: pidRegistry.js |
| `frontend/src/data/swarmControl.js` | SWARM project file: swarmControl.js |
| `frontend/src/main.jsx` | Vite entry that mounts the SWARM React application. |
| `frontend/src/pages/AIRecommendations.jsx` | SWARM plant-app page: AIRecommendations. |
| `frontend/src/pages/Alerts.jsx` | SWARM plant-app page: Alerts. |
| `frontend/src/pages/Analytics.jsx` | SWARM plant-app page: Analytics. |
| `frontend/src/pages/AuthCallback.jsx` | SWARM plant-app page: AuthCallback. |
| `frontend/src/pages/ChangePassword.jsx` | SWARM plant-app page: ChangePassword. |
| `frontend/src/pages/ConnectDevice.jsx` | SWARM plant-app page: ConnectDevice. |
| `frontend/src/pages/Dashboard.jsx` | Plant live-monitoring dashboard and empty states. |
| `frontend/src/pages/Help.jsx` | SWARM plant-app page: Help. |
| `frontend/src/pages/Login.jsx` | Email/password (and optional SSO) sign-in screen. |
| `frontend/src/pages/Maintenance.jsx` | SWARM plant-app page: Maintenance. |
| `frontend/src/pages/Notifications.jsx` | SWARM plant-app page: Notifications. |
| `frontend/src/pages/Partners.jsx` | SWARM plant-app page: Partners. |
| `frontend/src/pages/PlantHmi.jsx` | SWARM plant-app page: PlantHmi. |
| `frontend/src/pages/Plants.jsx` | SWARM plant-app page: Plants. |
| `frontend/src/pages/Reports.jsx` | SWARM plant-app page: Reports. |
| `frontend/src/pages/Sensors.jsx` | SWARM plant-app page: Sensors. |
| `frontend/src/pages/Settings.jsx` | SWARM plant-app page: Settings. |
| `frontend/src/pages/Signup.jsx` | SWARM plant-app page: Signup. |
| `frontend/src/pages/Users.jsx` | SWARM plant-app page: Users. |
| `frontend/src/pages/control-center/Approvals.jsx` | Control Center page for tenant onboarding and plant assignment. |
| `frontend/src/pages/control-center/Companies.jsx` | Control Center page for tenant onboarding and plant assignment. |
| `frontend/src/pages/control-center/CompanyDetail.jsx` | Control Center page for tenant onboarding and plant assignment. |
| `frontend/src/pages/control-center/HmiAccess.jsx` | Control Center page for tenant onboarding and plant assignment. |
| `frontend/src/pages/control-center/Overview.jsx` | Control Center page for tenant onboarding and plant assignment. |
| `frontend/src/pages/control-center/PlantInventory.jsx` | Control Center plant inventory, including the unassigned-pool tab. |
| `frontend/src/services/api.js` | Axios API client used by non-NOVA SWARM pages. |
| `frontend/src/styles/responsive.css` | SWARM project file: responsive.css |
| `frontend/src/theme.js` | MUI theme tokens for the SWARM visual identity. |
| `frontend/src/utils/constants.js` | Frontend helper used by SWARM pages (constants). |
| `frontend/src/utils/espClient.js` | Frontend helper used by SWARM pages (espClient). |
| `frontend/src/utils/homeRoute.js` | Frontend helper used by SWARM pages (homeRoute). |
| `frontend/src/utils/networkError.js` | Frontend helper used by SWARM pages (networkError). |
| `frontend/src/utils/responsive.js` | Frontend helper used by SWARM pages (responsive). |
| `frontend/src/utils/sso.js` | Frontend helper used by SWARM pages (sso). |
| `frontend/vite.config.js` | SWARM project file: vite.config.js |
| `railway.toml` | SWARM project file: railway.toml |
| `releases/README.md` | SWARM project file: README.md |
| `scripts/add-purpose-comments.py` | Adds PURPOSE comments to retained non-NOVA files. |
| `scripts/deploy-rpi.sh` | Pulls git and rebuilds the SWARM Docker stack on a device. |
| `scripts/install-rpi-autostart.sh` | Operational script for SWARM deployment or maintenance. |
| `scripts/monochrome-svg.py` | Operational script for SWARM deployment or maintenance. |
| `scripts/railway-api.sh` | Operational script for SWARM deployment or maintenance. |
| `scripts/swarm-rpi.service` | Operational script for SWARM deployment or maintenance. |
| `scripts/verify-partner-api.sh` | Operational script for SWARM deployment or maintenance. |
