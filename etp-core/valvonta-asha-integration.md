# Asianhallinta-integraation kuvaus

Energiatodistusrekisteri on integroitu kolmannen osapuolen asianhallintajärjestelmän kanssa. Korkeamman tason kuvaus ja
linkkejä muuhun dokumentaation Confluencessa https://knowledge.solita.fi/display/AE/Asianhallinta+ASHA.

Tämä dokumentti pyrkii kuvaamaan järjestelmän toiminnan tarkemmalla tasolla

## log-toimenpide! funktion logiikka

### Käsitteistöä

| Käsite            | Merkitys                                                                                                                        |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------|
| Valvonta          | Ylätason käsite. Esimerkiksi myynnissä olevasta kohteesta ilman energiatodistusta voidaan aloittaa valvonta                     |
| ETP               | Energiatodistusrekisteri                                                                                                        |
| Asha              | Asianhallintajärjestelmä, johon valvonnan tila ja dokumentit tallennetaan. Myös ETP tallentaa toimenpiteet, muttei dokumentteja |
| Toimenpide (ETP)  | Valvonnan osa, josta tulee merkintä myös Ashaan. Ashassa osatoimenpide. Kuuluu Ashassa aina Ashan toimenpiteeseen               |
| Toimenpide (Asha) | Valvonnan osa. Voi sisältää useita osatoimenpiteitä, eli ETP:n toimepiteitä                                                     |
| Päätös            | Ashan operaatio, jolla toimenpide lopetetaan ja siirrytään seuraavaan                                                           |
| Toimenpiteen tila | Ashan toimenpiteen tila, mahdollisia arvoja esimerkiksi NEW tai READY                                                           |

```mermaid
sequenceDiagram 
    autonumber
    box Energiatodistusrekisteri
        participant etp_front as Frontend
        participant etp as Backend
        participant etp_db as PostgreSQL
    end
    participant asha as Asianhallinta
    etp_front ->> etp: Luo ETP toimenpide
    etp ->> etp_db: Tallenna ETP toimenpide tietokantaan
    loop : Hae kaikkien mahdollisten Ashan toimenpiteiden tila
        etp ->> asha: Hae tila toimenpiteelle
        note left of etp: Ashan toimenpiteet: <br/> Vireillepano <br/> Käsittely <br/> Päätöksenteko <br/> Tiedoksianto ja toimeenpano <br/> Valitusajan umpeutuminen
        note left of etp: Toimenpiteen mahdolliset tilat: <br/> NEW <br/> WAITING <br/> PROCESSING <br/> READY <br/> SUSPENDED <br/> TRANSFERRED <br/> INVALIDATED <br/> CLOSED
        asha ->> etp: Vastaus
    end
    note over etp: Esimerkki valvonnan tilasta: [{"Vireillepano" "READY"} {"Käsittely" "NEW"}] <br/> Toimenpiteet, joita valvonnalla ei Ashassa ei ole eivät näy vastauksessa
    etp ->> etp: Vaihdetaan osatoimenpiteen Ashan toimenpide valvonnan tilan perusteella
    note left of etp: Vireillepano on aina vaadittu. <br/> Jos tila on {"Vireillepano" "NEW"}, <br/> vaihdetaan osatoimenpiteen toimenpiteeksi "Vireillepano"
    note left of etp: Etp on koodattu siten että valvonnan toimenpiteet seuraavat <br/> Ashassa toisiaan määrätyssä järjestyksessä. <br/> (vastoin tämänhetkistä Ashan prosessia)<br/> Jos toimenpiteen haluttu vaihe on aiempi kuin Ashan tila, <br/> siirretään toimenpide myöhempään vaiheeseen
    alt : Mikäli haluttu toimenpide ei ole vielä avattu Ashassa <br/> (puuttuu valvonnan vaiheista Ashassa) <br/> ja siirtymä on tuettu
        etp ->> asha: Avataan Ashassa uusi toimenpide lähettämällä Ashaan vastaava päätös
    end
    etp ->> asha: Aloita asian käsittely
    etp ->> asha: Luo haluttu osatoimenpide Ashaan
    loop : Lisää kaikki dokumentit
        etp ->> asha: Lisää dokumentti
    end
    etp ->> asha: Aloita osatoimenpiteen käsittely
    etp ->> asha: Merkitse osatoimenpide valmiiksi
```
