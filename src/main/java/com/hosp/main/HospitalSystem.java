package com.hosp.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.hosp.records.DischargeSummary;
import com.hosp.records.EmergencyRoom;
import com.hosp.records.InPatient;
import com.hosp.records.Patient;
import com.hosp.records.Prescription;
import com.hosp.records.Room;
import com.hosp.records.RoomManager;
import com.hosp.records.Ward;
import com.hosp.staff.Doctor;
import com.hosp.staff.Nurse;
import com.hosp.utils.AppException;
import com.hosp.utils.FileManager;
import com.hosp.utils.Validation;

public class HospitalSystem 
{
    private final Scanner sc = new Scanner(System.in);
    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Nurse> nurses = new ArrayList<>();
    private final RoomManager roomManager = new RoomManager();
    private final EmergencyRoom er = new EmergencyRoom();

    // Data paths
    private final Path dataDir = Paths.get("data");
    private final Path patientsJson = dataDir.resolve("patients.json");
    private final Path doctorsJson = dataDir.resolve("doctors.json");
    private final Path excelReport = dataDir.resolve("hospital_report.xlsx");

    public static void main(String[] args) 
    {
        HospitalSystem app = new HospitalSystem();
        app.initSampleData();
        app.run();
    }

    private void initSampleData() 
    {
        // doctors
        doctors.add(new Doctor("D001", "Dr. Saksham", "General Medicine", "9876543210", true));
        doctors.add(new Doctor("D002", "Dr. Asha", "Cardiology", "9876501234", true));
        doctors.add(new Doctor("D003", "Dr. Meena", "Pediatrics", "9876512345", true));
        doctors.add(new Doctor("D004", "Dr. Rohan", "Neurology", "9876523456", false));
        doctors.add(new Doctor("D005", "Dr. Priya", "Orthopedics", "9876534567", true));
        doctors.add(new Doctor("D006", "Dr. Karan", "Dermatology", "9876545678", false));
        doctors.add(new Doctor("D007", "Dr. Neha", "Gastroenterology", "9876556789", true));
        doctors.add(new Doctor("D008", "Dr. Rahul", "Psychiatry", "9876567890", true));

        // nurses
        nurses.add(new Nurse("N001", "Nurse Rina", "9998887776"));

        // wards and rooms
        Ward general = new Ward("Ward-A", "General");
        general.addRoom(new Room("R101", false, 2));
        general.addRoom(new Room("R102", false, 2));

        Ward icu = new Ward("Ward-ICU", "ICU");
        icu.addRoom(new Room("R201", true, 1));
        icu.addRoom(new Room("R202", true, 1));

        roomManager.addWard(general);
        roomManager.addWard(icu);

        // initial patients (demo)
        Patient p1 = new InPatient("P001", "Mansi", 22, "F", LocalDate.now(), 2, 1000.0);
        p1.setDiagnosis("Fever");
        patients.add(p1);
    }

    public void run() 
    {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            try 
            {
                switch (choice) 
                {
                    case "1": admitPatientInteractive(false); break;
                    case "2": admitPatientInteractiveER(); break;
                    case "3": assignDoctorInteractive(); break;
                    case "4": addPrescriptionInteractive(); break;
                    case "5": processNextER(); break;
                    case "6": dischargePatientInteractive(); break;
                    case "7": showOccupancySummary(); break;
                    case "8": saveAll(); break;
                    case "9": loadAll(); break;
                    case "10": exportExcelReport(); break;
                    case "0": running=false; break;
                    default: System.out.println("Invalid choice");
                }
            } catch (AppException e) 
            {
                System.out.println("Application error: " + e.getMessage());
            } catch (Exception e) 
            {
                System.out.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Exiting. Bye!");
    }

    private void printMenu() 
    {
        System.out.println("\n--- Hospital System Menu ---");
        System.out.println("1. Admit Patient (Normal)");
        System.out.println("2. Admit Patient via ER (Triage)");
        System.out.println("3. Assign Doctor to Patient");
        System.out.println("4. Add Prescription");
        System.out.println("5. Process next ER patient");
        System.out.println("6. Discharge Patient");
        System.out.println("7. Show Occupancy Summary");
        System.out.println("8. Save all data (JSON)");
        System.out.println("9. Load data (JSON)");
        System.out.println("10. Export Excel Report");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    // ---------- Actions ----------
    private void admitPatientInteractive(boolean isER) throws AppException 
    {
        System.out.print("Enter name: "); String name = sc.nextLine().trim();
        System.out.print("Enter age: "); int age = Integer.parseInt(sc.nextLine().trim());
        Validation.validateAge(age);
        System.out.print("Enter gender: "); String gender = sc.nextLine().trim();
        String id = "P" + (System.currentTimeMillis() % 100000);
        if (!isER) {
            System.out.print("Room charge per day (e.g., 1000): ");
            double charge = Double.parseDouble(sc.nextLine().trim());
            InPatient ip = new InPatient(id, name, age, gender, LocalDate.now(), 1, charge);
            patients.add(ip);
            System.out.println("Admitted InPatient: " + ip.brief());
            // try assign a bed automatically
            RoomManager.AssignedBed ab = roomManager.findAndAssignBed(id, false);
            if (ab != null) {
                ip.setAssignedBedId(ab.bed.getBedId());
                System.out.println("Assigned bed: " + ab);
            } else {
                System.out.println("No bed available at moment. Patient waiting.");
            }
        } else 
        {
            // ER admission will call admitPatientInteractiveER
            System.out.println("Use ER admission for triage.");
        }
    }

    private void admitPatientInteractiveER() throws AppException 
    {
        System.out.print("Enter name: "); String name = sc.nextLine().trim();
        System.out.print("Enter age: "); int age = Integer.parseInt(sc.nextLine().trim());
        Validation.validateAge(age);
        System.out.print("Enter gender: "); String gender = sc.nextLine().trim();
        System.out.print("Enter triage severity (1-10): "); int severity = Integer.parseInt(sc.nextLine().trim());
        String id = "P" + (System.currentTimeMillis() % 100000);
        InPatient ip = new InPatient(id, name, age, gender, LocalDate.now(), 1, 1500.0);
        patients.add(ip);
        er.arrive(id, severity);
        System.out.println("ER arrival recorded. Patient ID: " + id + " Severity: " + severity);
    }

    private void assignDoctorInteractive() 
    {
        System.out.print("Enter patient id: "); String pid = sc.nextLine().trim();
        Optional<Patient> opt = patients.stream().filter(p -> p.getId().equals(pid)).findFirst();
        if (!opt.isPresent()) { System.out.println("Patient not found"); return; }
        System.out.println("Available doctors:");
        doctors.forEach(System.out::println);
        System.out.print("Enter doctor id to assign: "); String did = sc.nextLine().trim();
        Optional<Doctor> od = doctors.stream().filter(d -> d.getId().equals(did)).findFirst();
        if (!od.isPresent()) { System.out.println("Doctor not found"); return; }
        Patient p = opt.get();
        p.setAssignedDoctorId(did);
        od.get().setAvailable(false);
        System.out.println("Assigned " + od.get().getName() + " to " + p.getName());
    }

    private void addPrescriptionInteractive() 
    {
        System.out.print("Enter patient id: "); String pid = sc.nextLine().trim();
        Optional<Patient> opt = patients.stream().filter(p -> p.getId().equals(pid)).findFirst();
        if (!opt.isPresent()) { System.out.println("Patient not found"); return; }
        Patient p = opt.get();
        System.out.print("Medicine name: "); String med = sc.nextLine().trim();
        System.out.print("Dosage (e.g., 500mg): "); String dose = sc.nextLine().trim();
        System.out.print("Duration (days): "); int days = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Estimated cost: "); double cost = Double.parseDouble(sc.nextLine().trim());
        Prescription pr = new Prescription(med, dose, days, cost);
        addPrescription(p, pr);
        System.out.println("Prescription added: " + pr);
    }

    // Objects as arguments (P8)
    public void addPrescription(Patient patient, Prescription prescription) 
    {
        if (patient == null || prescription == null) { System.out.println("invalid"); return; }
        patient.addPrescription(prescription);
    }

    private void processNextER() 
    {
    EmergencyRoom.ERPatient peek = er.peekNext();
    if (peek == null) {
        System.out.println("No ER patients waiting");
        return;
    }
    System.out.println("Next patient in ER queue is: " + peek.patientId + " (Severity: " + peek.severity + ")");
    EmergencyRoom.ERPatient ep = er.nextToProcess();
    if (ep == null) { // unlikely because we peeked, but safe
        System.out.println("No ER patients waiting");
        return;
    }
    System.out.println("Processing ER patient: " + ep.patientId + " severity: " + ep.severity);
    boolean needsICU = ep.severity >= 8;
    RoomManager.AssignedBed ab = roomManager.findAndAssignBed(ep.patientId, needsICU);
    Optional<Patient> opt = patients.stream().filter(p -> p.getId().equals(ep.patientId)).findFirst();
    if (opt.isPresent() && ab != null) {
        Patient p = opt.get();
        if (p instanceof InPatient) ((InPatient)p).setAssignedBedId(ab.bed.getBedId());
        System.out.println("Assigned bed: " + ab);
    } else if (ab == null) {
        System.out.println("No suitable bed. Keep patient in ER waiting or arrange transfer.");
    } else {
        System.out.println("No patient object found for id: " + ep.patientId);
    }
    }


    private void dischargePatientInteractive() 
    {
        System.out.print("Enter patient id to discharge: "); String pid = sc.nextLine().trim();
        Optional<Patient> op = patients.stream().filter(p -> p.getId().equals(pid)).findFirst();
        if (!op.isPresent()) { System.out.println("Patient not found"); return; }
        Patient p = op.get();
        String doctorName = "Unknown";
        if (p.getAssignedDoctorId() != null) {
            Optional<Doctor> d = doctors.stream().filter(doc -> doc.getId().equals(p.getAssignedDoctorId())).findFirst();
            doctorName = d.map(Doctor::getName).orElse("Unknown");
            d.ifPresent(doc -> doc.setAvailable(true)); // free doctor
        }
        double total = p.calculateCost();
        DischargeSummary s = new DischargeSummary(p.getId(), p.getName(), doctorName, p.getDiagnosis()==null?"-":p.getDiagnosis(), total);
        try {
            Path out = dataDir.resolve("discharge_" + p.getId() + ".txt");
            FileManager.saveDischargeSummary(s, out);
            System.out.println("Discharge summary saved to " + out.toString());
        } catch (Exception e) {
            System.out.println("Failed to save summary: " + e.getMessage());
        }
        // vacate bed if assigned
        if (p instanceof InPatient) {
            String bid = ((InPatient)p).getAssignedBedId();
            if (bid != null) {
                // find bed
                for (Ward w : roomManager.getWards()) {
                    for (Room r : w.getRooms()) {
                        if (r.vacateBed(bid)) { System.out.println("Vacated bed " + bid); break; }
                    }
                }
            }
        }
        // remove from active list
        patients.remove(p);
    }

    private void showOccupancySummary() 
    {
        System.out.println("=== Facility Occupancy Summary ===");
        for (Ward w : roomManager.getWards()) {
            System.out.println(w);
            for (Room r : w.getRooms()) System.out.println("  " + r);
        }
        int totalBeds = roomManager.getWards().stream().mapToInt(Ward::totalBeds).sum();
        int freeBeds = roomManager.getWards().stream().mapToInt(Ward::freeBeds).sum();
        System.out.println("Total beds: " + totalBeds + " | Free beds: " + freeBeds);
        System.out.println("ER queue size: " + er.queueSize());
    }

    private void saveAll() 
    {
        try {
            Files.createDirectories(dataDir);
            FileManager.savePatientsJson(patients, patientsJson);
            FileManager.saveDoctorsJson(doctors, doctorsJson);
            System.out.println("Saved JSON files to " + dataDir.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAll() 
    {
        try {
            List<Patient> loaded = FileManager.loadPatientsJson(patientsJson);
            if (loaded != null && !loaded.isEmpty()) {
                patients.clear();
                patients.addAll(loaded);
            }
            List<Doctor> dloaded = FileManager.loadDoctorsJson(doctorsJson);
            if (dloaded != null && !dloaded.isEmpty()) {
                doctors.clear();
                doctors.addAll(dloaded);
            }
            System.out.println("Loaded data from JSON. Patients: " + patients.size() + " Doctors: " + doctors.size());
        } catch (Exception e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }

    private void exportExcelReport() 
    {
        try {
            Files.createDirectories(dataDir);
            FileManager.writeExcelReport(patients, doctors, roomManager, excelReport);
            System.out.println("Excel report exported to " + excelReport.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to export Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }
}