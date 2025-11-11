package com.hosp.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.hosp.records.*;
import com.hosp.staff.Doctor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class FileManager {
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public JsonElement serialize(LocalDate date, java.lang.reflect.Type type, JsonSerializationContext context) {
                return new JsonPrimitive(date.toString());
            }
        })
        .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
                return LocalDate.parse(json.getAsString());
            }
        })
        .setPrettyPrinting()
        .create();

    private static final DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // JSON persistence
    public static void savePatientsJson(List<Patient> patients, Path path) throws IOException {
        try (Writer w = Files.newBufferedWriter(path)) {
            gson.toJson(patients, w);
        }
    }

    public static List<Patient> loadPatientsJson(Path path) throws IOException {
        if (!Files.exists(path)) return new ArrayList<>();
        try (Reader r = Files.newBufferedReader(path)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            List<Patient> list = new ArrayList<>();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                if (obj.has("admitDate") || obj.has("assignedBedId")) {
                    list.add(gson.fromJson(obj, InPatient.class));
                } else if (obj.has("visitDate")) {
                    list.add(gson.fromJson(obj, OutPatient.class));
                }
            }
            return list;
        } catch (Exception e) {
            throw new IOException("Failed to load patients JSON", e);
        }
    }

    // Save doctors as JSON
    public static void saveDoctorsJson(List<Doctor> doctors, Path path) throws IOException {
        try (Writer w = Files.newBufferedWriter(path)) {
            gson.toJson(doctors, w);
        }
    }

    public static List<Doctor> loadDoctorsJson(Path path) throws IOException {
        if (!Files.exists(path)) return new ArrayList<>();
        try (Reader r = Files.newBufferedReader(path)) {
            Type listType = new TypeToken<List<Doctor>>(){}.getType();
            return gson.fromJson(r, listType);
        }
    }

    // Write discharge summary text file
    public static void saveDischargeSummary(DischargeSummary s, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(s.generateSummaryText());
        }
    }

    // Write Excel workbook with multiple sheets: Patients, Doctors, Wards/Rooms/Beds
    public static void writeExcelReport(List<Patient> patients, List<Doctor> doctors, RoomManager roomManager, Path excelPath) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            // Patients sheet
            Sheet pSheet = wb.createSheet("Patients");
            Row header = pSheet.createRow(0);
            String[] pcols = {"Patient ID", "Name", "Age", "Gender", "Type", "AssignedDoctor", "AssignedBed", "Diagnosis", "TotalCost"};
            for (int i = 0; i < pcols.length; i++) header.createCell(i).setCellValue(pcols[i]);
            int r = 1;
            for (Patient p : patients) {
                Row row = pSheet.createRow(r++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getName());
                row.createCell(2).setCellValue(p.getAge());
                row.createCell(3).setCellValue(p.getGender());
                row.createCell(4).setCellValue(p.getClass().getSimpleName());
                row.createCell(5).setCellValue(p.getAssignedDoctorId() == null ? "" : p.getAssignedDoctorId());
                String assignedBed = "";
                if (p instanceof InPatient) assignedBed = ((InPatient) p).getAssignedBedId() == null ? "" : ((InPatient) p).getAssignedBedId();
                row.createCell(6).setCellValue(assignedBed);
                row.createCell(7).setCellValue(p.getDiagnosis() == null ? "" : p.getDiagnosis());
                row.createCell(8).setCellValue(p.calculateCost());
            }

            // Doctors sheet
            Sheet dSheet = wb.createSheet("Doctors");
            Row dh = dSheet.createRow(0);
            String[] dcols = {"DoctorID", "Name", "Specialization", "Contact", "Available"};
            for (int i = 0; i < dcols.length; i++) dh.createCell(i).setCellValue(dcols[i]);
            r = 1;
            for (Doctor d : doctors) {
                Row row = dSheet.createRow(r++);
                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getName());
                row.createCell(2).setCellValue(d.getSpecialization());
                row.createCell(3).setCellValue(d.getContactNumber());
                row.createCell(4).setCellValue(d.isAvailable());
            }

            // Wards/Rooms/Beds sheet
            Sheet wSheet = wb.createSheet("Facility");
            Row wh = wSheet.createRow(0);
            String[] wcols = {"WardID", "WardType", "RoomID", "IsICU", "BedID", "Occupied", "PatientID"};
            for (int i = 0; i < wcols.length; i++) wh.createCell(i).setCellValue(wcols[i]);
            r = 1;
            if (roomManager != null) {
                for (Ward ward : roomManager.getWards()) {
                    for (Room room : ward.getRooms()) {
                        for (Bed bed : room.getBeds()) {
                            Row row = wSheet.createRow(r++);
                            row.createCell(0).setCellValue(ward.getWardId());
                            row.createCell(1).setCellValue(ward.getWardType());
                            row.createCell(2).setCellValue(room.getRoomId());
                            row.createCell(3).setCellValue(room.isICU());
                            row.createCell(4).setCellValue(bed.getBedId());
                            row.createCell(5).setCellValue(bed.isOccupied());
                            row.createCell(6).setCellValue(bed.getPatientId() == null ? "" : bed.getPatientId());
                        }
                    }
                }
            }

            // Create folder & write workbook
            Files.createDirectories(excelPath.getParent());
            try (OutputStream os = Files.newOutputStream(excelPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                wb.write(os);
            }
        } catch (Exception e) {
            throw new IOException("Failed to write Excel report", e);
        }
    }
}