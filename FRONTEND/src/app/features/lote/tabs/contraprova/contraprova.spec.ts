import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Contraprova } from './contraprova';

describe('Contraprova', () => {
  let component: Contraprova;
  let fixture: ComponentFixture<Contraprova>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Contraprova],
    }).compileComponents();

    fixture = TestBed.createComponent(Contraprova);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
