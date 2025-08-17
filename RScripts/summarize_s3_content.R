#summarize_s3_conten is a function that can be used to get summary information about what camera deployments are uploaded on AWS s3.
#It reads the /misc_files/files_uploaded_to_s3.txt file and produces two summary dataframes in a list as output. The dataframes 
#are presented as sortable visual tables via the reactable package.

#The function takes one argument, the table of deployments, which is read-in as a single-column listing the file names
#of all the images on s3. The function extracts the camera_sd (i.e. deployment), Year, and Month for each file into new columns.

#The function then makes two summary tables based on that expanded data frame.

#The function returns a list with two tables of summary information. The reason for two tables of output is that, with some camera
#deployments, there are date issues associated with some files. For example, if a researcher sets up the camera and lets it begin
#taking pictures during camera set up, but has not yet checked that the date and time are correct on the camera, some of the files
#in the folder for that deployment will have different year information. (Example: C066_SD019 which has 6 images from "2020" during camera set up
#and then an additional 594 images once the date was correctly set on the camera during setup).

summarize_s3_content <- function(dataframe){
  require(reactable)
  require(reactablefmtr)
  require(tidyverse)
  
  #extract Cam_SD, Year and month from File_name
  
  dataframe <- dataframe |> mutate(
    Cam_SD = str_sub(File_name, 1, 10),
    Year = as.integer(str_sub(File_name,12, 15)),
    Month = as.integer(str_sub(File_name,16, 17))
  )
  
  #create first summary table, grouped by Cam_SD
  
  dataframe_by_cam_sd_summary <- dataframe |>
    group_by(Cam_SD) |>
    summarise(
      start_year = min(Year, na.rm = T),
      end_year = max(Year, na.rm = T), 
      low_month = min(Month, na.rm = T),
      high_month = max(Month, na.rm = T),
      num_months = length(unique(Month))
    )
  
  #create second summary dataframe, grouped by Cam_SD and Year
  dataframe_by_year_summary <- dataframe |>
    group_by(Cam_SD, Year) |>
    summarise(
      start_year = min(Year, na.rm = T),
      end_year = max(Year, na.rm = T), 
      low_month = min(Month, na.rm = T),
      high_month = max(Month, na.rm = T),
      num_months = length(unique(Month))
    )
  
  #create empty dataframe to receive results
  loop_output <- data.frame(
    Cam_SD = character(),
    Year = integer(),
    first_date = integer(),
    last_date = integer(),
    Num_images = integer()
  )
  
  #start loop that chunks uploaded by Cam_SD deployment and Year
  
  for (i in 1:nrow(dataframe_by_year_summary)){ #initialize loop across each Year instance of each Cam_SD
    #filter to one Cam_SD and one Year
    subset <- dataframe |> 
      filter(
        Cam_SD == dataframe_by_year_summary$Cam_SD[i] & Year == dataframe_by_year_summary$Year[i])
    
    #make sure files are in chronological order
    subset <- subset |> arrange(File_name, Year)
    
    #extract Cam_SD for this instance as variable
    ith_cam_sd <- unique(subset$Cam_SD)
    
    #extract Year for this instance as variable
    ith_year <- unique(subset$Year)
    
    #extract startdate for this instance as variable
    ith_startdate <- str_sub(subset$File_name[1], 12, 25)
    
    #extract enddate for this instance as variable
    ith_enddate <- str_sub(subset$File_name[nrow(subset)], 12, 25)
    
    #count and extract number of images in this instance
    ith_num_images <- nrow(subset)
    
    #store extracted variables in dataframe
    ith_output <- data.frame(
      Cam_SD = ith_cam_sd, 
      Year = ith_year, 
      first_date = ith_startdate, 
      last_date = ith_enddate, 
      Num_images = ith_num_images)
    
    #bind each iteration of loop into results storage dataframe
    loop_output <- rbind(loop_output, ith_output)
    
    #repeat for each Cam_SD/Year combination
  } #exit loop
  
  #assign date variables as date via lubridate
  loop_output$first_date <- ymd_hms(loop_output$first_date)
  loop_output$last_date <- ymd_hms(loop_output$last_date)
  
  #now create the reactable tables to output as list
  
  #first table
  table_1 <- reactable( 
    
    dataframe_by_cam_sd_summary[,1:3],
    
    columns = list(
      Cam_SD = colDef(name = "Deployment name"), #assign first col name
      start_year = colDef(name = "First year", align = "center"), #assign second col name
      end_year = colDef(name = "Last year", align = "center")),
    
    bordered = TRUE,
    striped = TRUE,
    highlight = TRUE,
    defaultSorted = c("Cam_SD"))
  
  table_1 <- table_1 |> 
    add_title("List of game camera deployments uploaded to s3")
  
  
  #second table
  table_2 <- reactable(
    loop_output,
    
    columns = list(
      Cam_SD = colDef(name = "Deployment name"),
      Year = colDef(name = "Deployment year"),
      first_date = colDef(name = "First deployed on", format = colFormat(date = TRUE)),
      last_date = colDef(name = "Last deployed on", format = colFormat(date = TRUE)),
      Num_images = colDef(name = "Number images produced")),
    
    bordered = TRUE,
    striped = TRUE,
    highlight = TRUE,
    defaultSorted = c("Cam_SD")
  )
  
  table_2 <- table_2 |> 
    add_title("Further information about uploads in case of date issues")
  
  #return two tables as list
  return(list(table1 = table_1, table2 = table_2))
}