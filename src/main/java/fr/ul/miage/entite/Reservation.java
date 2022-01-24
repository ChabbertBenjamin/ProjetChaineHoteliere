package fr.ul.miage.entite;

import fr.ul.miage.repository.DatabaseManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Reservation {

    private int id;
    private int idHotel;
    private int idRoom;
    private Date dateStart;
    private Date dateEnd;
    private double price;
    private int nbPeople;
    DatabaseManager dm = new DatabaseManager();

    public Reservation(int id, int idHotel, int idRoom, Date dateStart, Date dateEnd, double price, int nbPeople) {
        this.id = id;
        this.idHotel = idHotel;
        this.idRoom = idRoom;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.price = price;
        this.nbPeople = nbPeople;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public int getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(int idRoom) {
        this.idRoom = idRoom;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getNbPeople() {
        return nbPeople;
    }

    public void setNbPeople(int nbPeople) {
        this.nbPeople = nbPeople;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idHotel=" + idHotel +
                ", idRoom=" + idRoom +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", price=" + price +
                ", nbPeople=" + nbPeople +
                '}';
    }

    private boolean isDateDuringWeekend(LocalDate date) {
        return (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7);
    }

    private boolean isDateDuringHighSeason(LocalDate date) throws SQLException {
        int highSeasonStartMonth = dm.getHighSeasonStartMonth();
        int highSeasonStartDay = dm.getHighSeasonStartDay();
        int highSeasonEndMonth = dm.getHighSeasonEndMonth();
        int highSeasonEndDay = dm.getHighSeasonEndDay();

        int monthToCheck = date.getMonthValue();
        int dayToCheck = date.getDayOfMonth();

        return ((monthToCheck >= highSeasonStartMonth && monthToCheck <= highSeasonEndMonth) && (dayToCheck >= highSeasonStartDay && dayToCheck <= highSeasonEndDay));
    }

    private boolean isDateDuringLowSeason(LocalDate date) {
        return !isDateDuringWeekend(date);
    }

    private List<LocalDate> getDatesBetween(Date startDate, Date endDate) {
        LocalDate localDateStart = startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate localDateEnd = endDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDateStart.datesUntil(localDateEnd)
                .collect(Collectors.toList());
    }

    //Apply day by day the weekendPrice
    private double calculatePriceBasedOnDatesCheckingWeekends() throws SQLException {
        List<LocalDate> reservationAllDates = getDatesBetween(this.dateStart, this.dateEnd);
        double priceBasedOnDates = 0;
        Room room = dm.getRoomById(this.idRoom);

        for (LocalDate localDate : reservationAllDates) {
            if (isDateDuringWeekend(localDate)) {
                priceBasedOnDates += room.getWeekendPrice();
            } else
            {
                priceBasedOnDates += room.getWeekPrice();
            }
        }
        return priceBasedOnDates;
    }

    //Apply day by day the seasonPrice
    private double calculatePriceBasedOnDatesCheckingSeason() throws SQLException {
        List<LocalDate> reservationAllDates = getDatesBetween(this.dateStart, this.dateEnd);
        double priceBasedOnDates = 0;
        Room room = dm.getRoomById(this.idRoom);

        for (LocalDate localDate : reservationAllDates) {
            if (isDateDuringHighSeason(localDate)) {
                priceBasedOnDates += room.getHighSeasonPrice();
            } else
            {
                priceBasedOnDates += room.getLowSeasonPrice();
            }
        }
        return priceBasedOnDates;
    }

    private Date LocalDateToDate(LocalDate localDate) {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }

    public boolean isNbResaInTwoWeeksUnder60Percent() throws SQLException {
        int nbResa = dm.getNbReservationInTwoWeeksByHotel(this.idHotel);
        Hotel hotel = dm.getHotelById(this.idHotel);
        return (nbResa * 100 / hotel.getNbRoom()) < 60;
    }

    public void applyLackOfReservationPromotion() throws SQLException {
        if(isNbResaInTwoWeeksUnder60Percent()) {
            dm.applyLackOfReservationPromotion(this.idHotel);
        }
    }
}
