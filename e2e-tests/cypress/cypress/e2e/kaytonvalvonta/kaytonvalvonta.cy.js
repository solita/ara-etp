import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';

const FIXTURES = {
  headers: paakayttajaHeaders
};

/**
 *
 * @param {String} toimenpideToCreate Name of the toimenpidetype to be created
 * @param {String[]} allowedToimenpidetypes Names of all the toimenpidetypes that should be allowed to be created
 * @param {String} primaryToimenpide Name of the toimenpidetype that should be highlighted as the most probably next toimenpide
 */
const openToimenpideCreationAndCheckAllowedToimenpidetypes = (
  toimenpideToCreate,
  allowedToimenpidetypes,
  primaryToimenpide
) => {
  cy.get('[data-cy="toimenpide-type-selection"]')
    .click()
    .parent()
    .within(() => {
      // Number of available options should be the count of allowed toimenpidetypes and the default 'Ei valintaa' option
      cy.get('li').should('have.length', allowedToimenpidetypes.length + 1);

      allowedToimenpidetypes.map(toimenpidetype => {
        cy.contains(toimenpidetype).should('be.visible');
      });

      cy.contains(primaryToimenpide).should('have.class', 'primary-option');

      cy.contains(toimenpideToCreate).click();
    });
};

const submitToimenpide = () => {
  cy.get('[data-cy="submit-button"]').click();
};

const checkToimenpideCreationSucceeded = () => {
  cy.get('.alert').contains('Toimenpide on lähetetty.');
  // Wait until the notification disappears before continuing
  cy.get('.alert').should('not.exist');
};

const toimenpideIsInCreatedToimenpideList = toimenpide => {
  cy.contains(toimenpide).should('be.visible');
};

const commentIsInToimenpideList = comment => {
  cy.contains(comment).should('be.visible');
};

const createKaytonvalvonta = () => {
  // Fill in minimum information
  cy.get('[data-cy="kohde.katuosoite"]')
    .type('Testikatu 26')
    .should('have.value', 'Testikatu 26')
    .blur();
  cy.get('[data-cy="kohde.postinumero"]').type('90100').blur();

  // Create the valvonta
  cy.get('[data-cy="-submit"]').click();

  // Add an omistajaosapuoli to the valvonta
  cy.get('[data-cy="Uusi henkilö"]').click();
  cy.get('[data-cy="henkilo.etunimi"]')
    .type('Enni')
    .should('have.value', 'Enni');
  cy.get('[data-cy="henkilo.sukunimi"]')
    .type('Esimerkki')
    .should('have.value', 'Esimerkki');
  cy.selectInSelect('henkilo.rooli-id', 'Omistaja');
  cy.get('[data-cy="-submit"]').click();
};

const startValvonta = () => {
  cy.get('[data-cy="start-button"]').click();
  cy.get('[data-cy="toimenpide.description"]')
    .type('Aloitetaan valvonta')
    .should('have.value', 'Aloitetaan valvonta')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Valvonnan aloitus');
  commentIsInToimenpideList('Aloitetaan valvonta');
};

const createKehotus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Kehotus',
    ['Kehotus', 'Valvonnan lopetus'],
    'Kehotus'
  );
  cy.selectInSelect('document-selector', 'Kehotus');
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Kehotus');
};

const createVaroitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Varoitus',
    ['Kehotus', 'Varoitus', 'Valvonnan lopetus'],
    'Varoitus'
  );
  cy.selectInSelect('document-selector', 'Varoitus');
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Varoitus');
};

const createKaskypaatosKuulemiskirje = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / kuulemiskirje',
    ['Kehotus', 'Varoitus', 'Käskypäätös / kuulemiskirje', 'Valvonnan lopetus'],
    'Käskypäätös / kuulemiskirje'
  );
  // Fine should have default value of 800 euros
  cy.get('[data-cy="toimenpide.fine"]')
    .should('have.value', 800)
    .clear()
    .type('9000')
    .should('have.value', 9000)
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Käskypäätös / kuulemiskirje');
};

const createKaskypaatosVarsinainenPaatos = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / varsinainen päätös',
    [
      'Käskypäätös / kuulemiskirje',
      'Käskypäätös / varsinainen päätös',
      'Valvonnan lopetus'
    ],
    'Käskypäätös / varsinainen päätös'
  );

  // Fine should have a default value of 9000 euros as it was set in the previous toimenpide
  cy.get('[data-cy="toimenpide.fine"]').should('have.value', 9000);

  cy.get('[data-cy="toimenpide.department-head-title-fi"]')
    .clear()
    .type('Ylijohtaja')
    .should('have.value', 'Ylijohtaja')
    .blur();

  cy.get('[data-cy="toimenpide.department-head-title-sv"]')
    .clear()
    .type('Överdirektör')
    .should('have.value', 'Överdirektör')
    .blur();

  cy.get('[data-cy="toimenpide.department-head-name"]')
    .clear()
    .type('Jonna Johtaja')
    .should('have.value', 'Jonna Johtaja')
    .blur();

  cy.selectInSelect(
    'recipient-answered-selector-0',
    'Asianosainen antoi vastineen kuulemiskirjeeseen'
  );

  cy.get('[data-cy="toimenpide.answer-commentary-fi-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti');

  cy.get('[data-cy="toimenpide.answer-commentary-sv-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti ruotsiksi')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti ruotsiksi');

  cy.get('[data-cy="toimenpide.statement-fi-0"]')
    .type('Tähän kirjoitetaan kannanotto.')
    .should('have.value', 'Tähän kirjoitetaan kannanotto.');
  cy.get('[data-cy="toimenpide.statement-sv-0"]')
    .type('Tähän kirjoitetaan kannanotto ruotsiksi.')
    .should('have.value', 'Tähän kirjoitetaan kannanotto ruotsiksi.');

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Helsingin hallinto-oikeus'
  );

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Käskypäätös / varsinainen päätös');
};

const closeValvontaMistakenly = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Valvonnan lopetus',
    [
      'Käskypäätös / varsinainen päätös',
      'Käskypäätös / tiedoksianto (ensimmäinen postitus)',
      'Valvonnan lopetus'
    ],
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)'
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type('Valvonta on suoritettu loppuun.')
    .should('have.value', 'Valvonta on suoritettu loppuun.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Valvonnan lopetus');
  commentIsInToimenpideList('Valvonta on suoritettu loppuun.');

  // Aloita valvonta button should not exist at this point
  cy.get('[data-cy="start-button"]').should('not.exist');
};

const createKaskypaatosTiedoksiantoEnsimmainenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)',
    [
      'Käskypäätös / varsinainen päätös',
      'Käskypäätös / tiedoksianto (ensimmäinen postitus)',
      'Valvonnan lopetus'
    ],
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetän tämän kirjeenä nyt.')
    .should('have.value', 'Lähetän tämän kirjeenä nyt.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (ensimmäinen postitus)'
  );
  commentIsInToimenpideList('Lähetän tämän kirjeenä nyt.');
};

const createKaskypaatosTiedoksiantoToinenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (toinen postitus)',
    [
      'Käskypäätös / tiedoksianto (toinen postitus)',
      'Käskypäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ],
    'Käskypäätös / tiedoksianto (toinen postitus)'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.')
    .should(
      'have.value',
      'Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (toinen postitus)'
  );
  commentIsInToimenpideList(
    'Lähetän tämän kirjeenä nyt uudestaan, kun se palautui lähettäjälle.'
  );
};

const createKaskypaatosTiedoksiantoHaastemies = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / tiedoksianto (Haastemies)',
    [
      'Käskypäätös / tiedoksianto (Haastemies)',
      'Käskypäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ],
    'Käskypäätös / tiedoksianto (Haastemies)'
  );

  cy.selectInSelect('karajaoikeus-selector-0', 'Ahvenanmaan käräjäoikeus');
  cy.get('[data-cy="haastemies-email-0"]')
    .type('haastemies@poliisitaikarajaoikeus.fi')
    .should('have.value', 'haastemies@poliisitaikarajaoikeus.fi')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / tiedoksianto (Haastemies)'
  );
};

const createKaskypaatosValitusajanOdotusJaUmpeutuminen = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Käskypäätös / valitusajan odotus ja umpeutuminen',
    ['Käskypäätös / valitusajan odotus ja umpeutuminen', 'Valvonnan lopetus'],
    'Käskypäätös / valitusajan odotus ja umpeutuminen'
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .should(
      'have.value',
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Käskypäätös / valitusajan odotus ja umpeutuminen'
  );
  commentIsInToimenpideList(
    'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
  );
};

const createHaOKasittely = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'HaO-käsittely',
    ['HaO-käsittely', 'Sakkopäätös / kuulemiskirje', 'Valvonnan lopetus'],
    'HaO-käsittely'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .should(
      'have.value',
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('HaO-käsittely');
  commentIsInToimenpideList(
    'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
  );
};

const createSakkopaatosKuulemiskirje = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / kuulemiskirje',
    ['Sakkopäätös / kuulemiskirje', 'Valvonnan lopetus'],
    'Sakkopäätös / kuulemiskirje'
  );

  // Fine should have a default value of 9000 euros as it was set previously
  // Change it now to another amount
  cy.get('[data-cy="toimenpide.fine"]')
    .should('have.value', 9000)
    .clear()
    .type('4500')
    .should('have.value', 4500)
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkopäätös / kuulemiskirje');
};

const createSakkopaatosVarsinainenPaatos = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / varsinainen päätös',
    [
      'Sakkopäätös / kuulemiskirje',
      'Sakkopäätös / varsinainen päätös',
      'Valvonnan lopetus'
    ],
    'Sakkopäätös / varsinainen päätös'
  );

  // Fine is now 4500 after setting it in the previous toimenpide
  cy.get('[data-cy="toimenpide.fine"]').should('have.value', 4500);

  // Department head information is what was previously used
  cy.get('[data-cy="toimenpide.department-head-title-fi"]').should(
    'have.value',
    'Ylijohtaja'
  );

  cy.get('[data-cy="toimenpide.department-head-title-sv"]').should(
    'have.value',
    'Överdirektör'
  );

  cy.get('[data-cy="toimenpide.department-head-name"]').should(
    'have.value',
    'Jonna Johtaja'
  );

  cy.selectInSelect(
    'recipient-answered-selector-0',
    'Asianosainen antoi vastineen kuulemiskirjeeseen'
  );

  // Notice about updating statement is visible after selecting the previous option
  cy.contains(
    'Tarkista ja muuta teksti kohdassa kannanotto vastineeseen'
  ).should('be.visible');

  cy.get('[data-cy="toimenpide.answer-commentary-fi-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti');

  cy.get('[data-cy="toimenpide.answer-commentary-sv-0"]')
    .type('Tähän kirjoitetaan vastineen kommentti ruotsiksi')
    .should('have.value', 'Tähän kirjoitetaan vastineen kommentti ruotsiksi');

  // Statement has default text filled in
  cy.get('[data-cy="toimenpide.statement-fi-0"]').should(
    'have.value',
    'ARAn päätökseen ei ole haettu muutosta, eli päätös on lainvoimainen. Maksuun tuomittavan uhkasakon määrä on sama kuin mitä se on ollut ARAn päätöksessä. ARAn näkemyksen mukaan uhkasakko tuomitaan maksuun täysimääräisenä, koska Asianosainen ei ole noudattanut päävelvoitetta lainkaan, eikä ole myöskään esittänyt noudattamatta jättämiselle pätevää syytä.'
  );
  cy.get('[data-cy="toimenpide.statement-sv-0"]').should(
    'have.value',
    'Ändring i ARAs beslut har inte sökts, dvs. beslutet har vunnit laga kraft. Vitesbeloppet som döms ut är detsamma som det var i ARAs beslut. Enligt ARAs uppfattning döms vitet ut till fullt belopp, eftersom Esimerkki inte alls har iakttagit huvudförpliktelsen och inte heller har angett någon giltig orsak till försummelsen.'
  );

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Pohjois-Suomen hallinto-oikeus'
  );

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkopäätös / varsinainen päätös');
};

const createSakkopaatosTiedoksiantoEnsimmainenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (ensimmäinen postitus)',
    [
      'Sakkopäätös / varsinainen päätös',
      'Sakkopäätös / tiedoksianto (ensimmäinen postitus)',
      'Valvonnan lopetus'
    ],
    'Sakkopäätös / tiedoksianto (ensimmäinen postitus)'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkopäätöksen ensimmäisen kerran.')
    .should('have.value', 'Lähetimme sakkopäätöksen ensimmäisen kerran.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (ensimmäinen postitus)'
  );
  commentIsInToimenpideList('Lähetimme sakkopäätöksen ensimmäisen kerran.');
};

const createSakkopaatosTiedoksiantoToinenPostitus = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (toinen postitus)',
    [
      'Sakkopäätös / tiedoksianto (toinen postitus)',
      'Sakkopäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ],
    'Sakkopäätös / tiedoksianto (toinen postitus)'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkopäätöksen toisen kerran.')
    .should('have.value', 'Lähetimme sakkopäätöksen toisen kerran.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (toinen postitus)'
  );
};

const createSakkopaatosTiedoksiantoHaastemies = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / tiedoksianto (Haastemies)',
    [
      'Sakkopäätös / tiedoksianto (Haastemies)',
      'Sakkopäätös / valitusajan odotus ja umpeutuminen',
      'Valvonnan lopetus'
    ],
    'Sakkopäätös / tiedoksianto (Haastemies)'
  );

  cy.selectInSelect(
    'administrative-court-selector-0',
    'Pohjois-Suomen hallinto-oikeus'
  );
  cy.selectInSelect('karajaoikeus-selector-0', 'Ahvenanmaan käräjäoikeus');
  cy.get('[data-cy="haastemies-email-0"]')
    .type('haastemies@poliisitaikarajaoikeus.fi')
    .should('have.value', 'haastemies@poliisitaikarajaoikeus.fi')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / tiedoksianto (Haastemies)'
  );
};

const createSakkopaatosValitusajanOdotusJaUmpeutuminen = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkopäätös / valitusajan odotus ja umpeutuminen',
    ['Sakkopäätös / valitusajan odotus ja umpeutuminen', 'Valvonnan lopetus'],
    'Sakkopäätös / valitusajan odotus ja umpeutuminen'
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .should(
      'have.value',
      'Odotamme valitusajan umpeutumista, josko saisimme vaikka valituksen sen puitteissa.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList(
    'Sakkopäätös / valitusajan odotus ja umpeutuminen'
  );
};

const createSakkopaatosHaOKasittely = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'HaO-käsittely',
    ['HaO-käsittely', 'Valvonnan lopetus', 'Sakkoluettelon lähetys menossa'],
    'HaO-käsittely'
  );

  cy.get('[data-cy="toimenpide.description"]')
    .type(
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .should(
      'have.value',
      'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
    )
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('HaO-käsittely');
  commentIsInToimenpideList(
    'Tämä tapaus meni hallinto-oikeuden käsittelyyn, joten odotamme ratkaisua.'
  );
};

const createSakkoluettelonLahetysMenossa = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Sakkoluettelon lähetys menossa',
    ['Sakkoluettelon lähetys menossa', 'Valvonnan lopetus'],
    'Sakkoluettelon lähetys menossa'
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type('Lähetimme sakkoluettelon.')
    .should('have.value', 'Lähetimme sakkoluettelon.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Sakkoluettelon lähetys menossa');
};

const closeValvonta = () => {
  openToimenpideCreationAndCheckAllowedToimenpidetypes(
    'Valvonnan lopetus',
    ['Valvonnan lopetus'],
    'Valvonnan lopetus'
  );
  cy.get('[data-cy="toimenpide.description"]')
    .type('Valvonta on suoritettu loppuun.')
    .should('have.value', 'Valvonta on suoritettu loppuun.')
    .blur();

  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Valvonnan lopetus');
  commentIsInToimenpideList('Valvonta on suoritettu loppuun.');
};

const reopenValvonta = () => {
  // After valvonta has been closed, there should be a button to reopen it if it was closed mistakenly
  cy.get('[data-cy="continue-button"]').click();

  cy.get('[data-cy="toimenpide.description"]')
    .type('Laitoin vahingossa kiinni, avataan uudestaan.')
    .should('have.value', 'Laitoin vahingossa kiinni, avataan uudestaan.')
    .blur();
  submitToimenpide();
  checkToimenpideCreationSucceeded();
  toimenpideIsInCreatedToimenpideList('Suljetun valvonnan uudelleenavaus');
  commentIsInToimenpideList('Laitoin vahingossa kiinni, avataan uudestaan.');

  // There's a warning telling to reopen the case in asha
  cy.contains(
    'Uudelleenavattu valvonta tulee avata erikseen myös asianhallinnassa ennen käsittelyn jatkamista.'
  ).should('be.visible');
};

context('Käytönvalvonta', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });

    cy.resetDb();
  });

  it('full process succeeds', () => {
    // Navigate to creating a new käytönvalvonta
    cy.visit('/');
    cy.contains('Käytön­valvon­nat').click();
    cy.get('[data-cy="Uusi valvonta"]').click();

    createKaytonvalvonta();

    // Navigate to the valvonta page
    cy.get('[data-cy^=KV]').click();
    cy.get('[data-cy=Valvonta]').click();

    startValvonta();

    createKehotus();

    createVaroitus();

    createKaskypaatosKuulemiskirje();

    createKaskypaatosVarsinainenPaatos();

    // Close the valvonta "mistakenly" at this point to test reopening a closed valvonta
    closeValvontaMistakenly();

    //Reopen the valvonta
    reopenValvonta();

    // Process should now continue where it was left off
    createKaskypaatosTiedoksiantoEnsimmainenPostitus();

    createKaskypaatosTiedoksiantoToinenPostitus();

    createKaskypaatosTiedoksiantoHaastemies();

    createKaskypaatosValitusajanOdotusJaUmpeutuminen();

    createHaOKasittely();

    createSakkopaatosKuulemiskirje();

    createSakkopaatosVarsinainenPaatos();

    createSakkopaatosTiedoksiantoEnsimmainenPostitus();

    createSakkopaatosTiedoksiantoToinenPostitus();

    createSakkopaatosTiedoksiantoHaastemies();

    createSakkopaatosValitusajanOdotusJaUmpeutuminen();

    createSakkopaatosHaOKasittely();

    createSakkoluettelonLahetysMenossa();

    closeValvonta();

    // Aloita valvonta button should not exist at this point
    cy.get('[data-cy="start-button"]').should('not.exist');
  });

  it('postinumero is a required field', () => {
    cy.visit('/');
    cy.contains('Käytön­valvon­nat').click();
    cy.get('[data-cy="Uusi valvonta"]').click();

    cy.get('[data-cy="kohde.katuosoite"]')
      .type('Testikatu 26')
      .should('have.value', 'Testikatu 26')
      .blur();

    cy.get('[data-cy="-submit"]').click();

    cy.get('.alert').contains(
      'Tiedoissa on virhe. Tarkista kohteen tiedot ennen lähettämistä.'
    );
    cy.get('[data-cy="kohde.postinumero"]').type('90100').blur();
    cy.get('[data-cy="-submit"]').click();
    cy.get('.alert').should('not.exist');
  });
});
