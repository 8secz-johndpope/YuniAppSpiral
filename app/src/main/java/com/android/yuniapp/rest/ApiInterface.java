package com.android.yuniapp.rest;


import com.android.yuniapp.model.CometRequestModel;
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.DeleteTaskReqModel;
import com.android.yuniapp.model.DocumentModel;
import com.android.yuniapp.model.GetAllTasksResponseModel;
import com.android.yuniapp.model.GetChatMessagingResponse;
import com.android.yuniapp.model.GetCometsResponseModel;
import com.android.yuniapp.model.GetDoucmentResponse;
import com.android.yuniapp.model.GetEntityDetailsResponse;
import com.android.yuniapp.model.GetMoonsResponseModel;
import com.android.yuniapp.model.GetMyQuoteResponseModel;
import com.android.yuniapp.model.GetPlanetsResponseModel;
import com.android.yuniapp.model.GetSatelliteResponseModel;
import com.android.yuniapp.model.GetLaunchedStarsResponseModel;
import com.android.yuniapp.model.GetStarStorageResponse;
import com.android.yuniapp.model.GetUnLaunchedStarsResponseModel;
import com.android.yuniapp.model.GetAuthTokenModel;
import com.android.yuniapp.model.GetLoginResponse;
import com.android.yuniapp.model.GetMessageModel;
import com.android.yuniapp.model.GetUserImagesResponse;
import com.android.yuniapp.model.MoonRequestModel;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.PlanetRequestModel;
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.SatelliteRequestModel;
import com.android.yuniapp.model.SatelliteResponseModel;
import com.android.yuniapp.model.StarRequestModel;
import com.android.yuniapp.model.StarResponseModel;
import com.android.yuniapp.model.TaskModel;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("user-login")
    Call<GetLoginResponse>
    login(@Field("email_id") String emailId,
          @Field("password") String password,
          @Field("social_id") String socialId,
          @Field("device_type") String deviceType,
          @Field("device_token") String deviceToken,
          @Field("auth_type") String authType);


    @FormUrlEncoded
    @POST("user-signup")
    Call<GetLoginResponse>
    register(@Field("name") String name,
             @Field("email_id") String emailId,
             @Field("address") String address,
             @Field("password") String password,
             @Field("profile_pic") String profilePic,
             @Field("social_id") String socialId,
             @Field("auth_type") String authType,
             @Field("device_type") String deviceType,
             @Field("device_token") String deviceToken);

    @FormUrlEncoded
    @POST("send-password-reset-code")
    Call<GetMessageModel>
    sendResetCode(@Field("email_id") String emailId);

    @FormUrlEncoded
    @POST("validate-password-reset-code")
    Call<GetAuthTokenModel>
    validateResetCode(@Field("email_id") String emailId,
                      @Field("reset_code") String resetCode);

    @FormUrlEncoded
    @POST("reset-password")
    Call<GetMessageModel>
    resetPassword(@Header("Auth-Token") String authToken,
                  @Field("password") String password);

    @Multipart
    @POST("update-user")
    Call<GetLoginResponse>
    updateProfile(@Header("Auth-Token") String auth_token,
                  @Part("name") RequestBody name,
                  @Part MultipartBody.Part file,
                  @Part("dob") RequestBody dob,
                  @Part("address") RequestBody address,
                  @Part("gender") RequestBody gender,
                  @Part("own_quote") RequestBody ownQuote,
                  @Part("automatic_quote") RequestBody automaticQuote);

    @FormUrlEncoded
    @POST("update-user")
    Call<GetLoginResponse>
    updateProfileWithoutMultipart(@Header("Auth-Token") String auth_token,
                                  @Field("name") String name,
                                  @Field("profile_pic") String profilePic,
                                  @Field("dob") String dob,
                                  @Field("address") String address,
                                  @Field("gender") String gender,
                                  @Field("own_quote") String ownQuote,
                                  @Field("automatic_quote") String automaticQuote);


    @GET("get-my-quotes")
    Call<GetMyQuoteResponseModel>
    getMyQuote(@Header("Auth-Token") String auth_token);


    @Multipart
    @POST("upload-document")
    Call<GetDoucmentResponse>
    uploadDocument(@Header("Auth-Token") String auth_token,
                   @Part MultipartBody.Part file);


    @POST("create-star")
    Call<StarResponseModel>
    createStar(@Header("Auth-Token") String auth_token,
               @Body StarRequestModel starRequestModel);


    @POST("create-planet")
    Call<PlanetResponseModel>
    createPlanet(@Header("Auth-Token") String auth_token,
                 @Body PlanetRequestModel planetRequestModel);


    @POST("create-moon")
    Call<MoonResponseModel>
    createMoon(@Header("Auth-Token") String auth_token,
               @Body MoonRequestModel moonRequestModel);

    @POST("create-satellite")
    Call<SatelliteResponseModel>
    createSatellite(@Header("Auth-Token") String auth_token,
                    @Body SatelliteRequestModel satelliteRequestModel);


    @POST("create-comet")
    Call<CometResponseModel>
    createComet(@Header("Auth-Token") String auth_token,
                @Body CometRequestModel cometRequestModel);


    @POST("update-star")
    Call<StarResponseModel>
    updateStar(@Header("Auth-Token") String auth_token,
               @Body StarRequestModel starRequestModel);


    @POST("update-planet")
    Call<PlanetResponseModel>
    updatePlanet(@Header("Auth-Token") String auth_token,
                 @Body PlanetRequestModel planetRequestModel);

    @POST("update-moon")
    Call<MoonResponseModel>
    updateMoon(@Header("Auth-Token") String auth_token,
               @Body MoonRequestModel moonRequestModel);

    @POST("update-satellite")
    Call<SatelliteResponseModel>
    updateSatellite(@Header("Auth-Token") String auth_token,
                    @Body SatelliteRequestModel satelliteRequestModel);


    @POST("update-comet")
    Call<CometResponseModel>
    updateComet(@Header("Auth-Token") String auth_token,
                @Body CometRequestModel cometRequestModel);


    @GET("get-launched-stars")
    Call<GetLaunchedStarsResponseModel>
    getLaunchedStars(@Header("Auth-Token") String auth_token);

    @GET("get-unlaunched-stars")
    Call<GetUnLaunchedStarsResponseModel>
    getUnLaunchedStars(@Header("Auth-Token") String auth_token);

    @GET("get-archive-stars")
    Call<GetUnLaunchedStarsResponseModel>
    getArchivedStars(@Header("Auth-Token") String auth_token);

    @FormUrlEncoded
    @POST("get-launched-planets")
    Call<GetPlanetsResponseModel>
    getLaunchedPlanets(@Header("Auth-Token") String auth_token,
                       @Field("star_id") String starId);

    @FormUrlEncoded
    @POST("get-unlaunched-planets")
    Call<GetPlanetsResponseModel>
    getUnLaunchedPlanets(@Header("Auth-Token") String auth_token,
                         @Field("star_id") String starId);


    @FormUrlEncoded
    @POST("get-archive-planets")
    Call<GetPlanetsResponseModel>
    getArchivedPlanets(@Header("Auth-Token") String auth_token,
                       @Field("star_id") String starId);

    @FormUrlEncoded
    @POST("get-launched-moons")
    Call<GetMoonsResponseModel>
    getLaunchedMoons(@Header("Auth-Token") String auth_token,
                     @Field("planet_id") String planetId);

    @FormUrlEncoded
    @POST("get-unlaunched-moons")
    Call<GetMoonsResponseModel>
    getUnLaunchedMoons(@Header("Auth-Token") String auth_token,
                       @Field("planet_id") String planetId);

    @FormUrlEncoded
    @POST("get-archive-moons")
    Call<GetMoonsResponseModel>
    getArchivedMoons(@Header("Auth-Token") String auth_token,
                     @Field("planet_id") String planetId);

    @FormUrlEncoded
    @POST("get-launched-satellites")
    Call<GetSatelliteResponseModel>
    getLaunchedSatellites(@Header("Auth-Token") String auth_token,
                          @Field("moon_id") String moonId);

    @FormUrlEncoded
    @POST("get-unlaunched-satellites")
    Call<GetSatelliteResponseModel>
    getUnLaunchedSatellites(@Header("Auth-Token") String auth_token,
                            @Field("moon_id") String moonId);


    @FormUrlEncoded
    @POST("get-archive-satellites")
    Call<GetSatelliteResponseModel>
    getArchivedSatellites(@Header("Auth-Token") String auth_token,
                          @Field("moon_id") String moonId);

    @GET("get-launched-comets")
    Call<GetLaunchedStarsResponseModel>
    getLaunchedComets(@Header("Auth-Token") String auth_token);

    @GET("get-unlaunched-comets")
    Call<GetCometsResponseModel>
    getUnLaunchedComets(@Header("Auth-Token") String auth_token);

    @GET("get-archive-comets")
    Call<GetCometsResponseModel>
    getArchivedComets(@Header("Auth-Token") String auth_token);


    @FormUrlEncoded
    @POST("delete-task")
    Call<GetMessageModel>
    deleteTask(@Header("Auth-Token") String auth_token,
               @Field("id") String id,
               @Field("type") String type);


    @POST("delete-task")
    Call<GetMessageModel>
    deleteStar(@Header("Auth-Token") String auth_token,
               @Body DeleteTaskReqModel deleteTaskReqModel);

    @FormUrlEncoded
    @POST("mark-task-completed")
    Call<GetMessageModel>
    markTaskDone(@Header("Auth-Token") String auth_token,
                 @Field("id") String id,
                 @Field("type") String type);

    @FormUrlEncoded
    @POST("get-star-storage")
    Call<GetStarStorageResponse>
    getStarStorage(@Header("Auth-Token") String auth_token,
                   @Field("star_id") String starId);

    @Multipart
    @POST("upload-user-images")
    Call<GetUserImagesResponse>
    callUploadUserImages(@Header("Auth-Token") String auth_token,
                         @Part List<MultipartBody.Part> images);


    @GET("get-user-images")
    Call<GetUserImagesResponse>
    callGetUserImages(@Header("Auth-Token") String auth_token);

    @FormUrlEncoded
    @POST("delete-document")
    Call<GetMessageModel>
    callDeleteDocument(@Header("Auth-Token") String auth_token,
                       @Field("document_id") String documentId,
                       @Field("document_url") String documentUrl);

    @FormUrlEncoded
    @POST("delete-user-image")
    Call<GetMessageModel>
    callDeleteUserImage(@Header("Auth-Token") String auth_token,
                        @Field("id") String id,
                        @Field("image_url") String imageUrl);

    @FormUrlEncoded
    @POST("send-chat-message")
    Call<GetChatMessagingResponse>
    callSendChatMessage(@Header("Auth-Token") String auth_token,
                        @Field("id") String id,
                        @Field("type") String type,
                        @Field("name") String name,
                        @Field("message") String message);


    @FormUrlEncoded
    @POST("get-chat-messages")
    Call<GetChatMessagingResponse>
    callGetChatMessage(@Header("Auth-Token") String auth_token,
                       @Field("id") String id,
                       @Field("type") String type);

    @FormUrlEncoded
    @POST("get-entity-details")
    Call<GetEntityDetailsResponse>
    getEntityDetails(@Header("Auth-Token") String auth_token,
                     @Field("id") String Id,
                     @Field("type") String type);


    @GET("get_all_users")
    Call<JsonElement>
    getUsersData(@Header("Token") String token);

    @POST("create-ordering")
    @FormUrlEncoded
    Call<JsonElement>
    createOrder(@Header("Auth-Token") String auth_token,
                @Field("object_id") String objectId,
                @Field("object_type") String objectType,
                @Field("display_order") String displayOrder,
                @Field("parameter") String parameter);


    @POST("get-all-task")
    @FormUrlEncoded
    Call<GetAllTasksResponseModel>
    getAllTask(@Header("Auth-Token") String auth_token,
               @Field("date") String date);

    @POST("create-task")
    @FormUrlEncoded
    Call<TaskModel>
    addTask(@Header("Auth-Token") String auth_token,
            @Field("date") String date,
            @Field("name") String name);

    @POST("update-task")
    @FormUrlEncoded
    Call<JsonElement>
    updateTask(@Header("Auth-Token") String auth_token,
               @Field("task_id") String taskId,
               @Field("date") String date,
               @Field("name") String name);

    @POST("delete-task-data")
    @FormUrlEncoded
    Call<JsonElement>
    deleteTask(@Header("Auth-Token") String auth_token,
               @Field("task_id") String taskId);


    @FormUrlEncoded
    @POST("send-video-call-notification")
    Call<JsonElement>
    sendVideoCallNottification(@Header("Auth-Token") String token,
                               @Field("object_id") String objectId,
                               @Field("type") String objectType,
                               @Field("chanel_name") String channelName);

    @FormUrlEncoded
    @POST("disconnect-video-call-notification")
    Call<JsonElement>
    sendDisconnectVideoCallNottification(@Header("Auth-Token") String token,
                                         @Field("object_id") String objectId,
                                         @Field("type") String objectType,
                                         @Field("chanel_name") String channelName);

    @FormUrlEncoded
    @POST("update-device-details")
    Call<JsonElement>
    updateDeviceDetails(@Header("Auth-Token") String auth_token,
                        @Field("device_type") String deviceType,
                        @Field("device_token") String deviceToken);
}

